package Util;

import Model.Lead;
import Model.Reclamo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Notificaciones automáticas al propietario:
 *   Canal 1 -> WhatsApp, a través de un microservicio local (carpeta wsp-service/,
 *              Node.js + Baileys). Es gratis y sin límite de mensajes, pero NO es
 *              la API oficial de WhatsApp: automatiza un número personal, lo cual
 *              viola los Términos de Servicio de WhatsApp y conlleva riesgo real
 *              (no garantizado) de que ese número sea bloqueado.
 *   Canal 2 -> Correo por la API HTTP de Resend (plan gratuito, 100 correos/día / 3000 al mes).
 *
 * Diseño:
 *  - Cada canal corre en un pool propio (asíncrono): el servlet responde
 *    al usuario sin esperar a WhatsApp ni al correo.
 *  - Cada tarea tiene su propio try/catch y reintentos: si un canal falla,
 *    el otro sigue y el lead/reclamo YA está guardado en Oracle (nunca se revierte).
 *  - Ningún método público lanza excepciones.
 *  - Las credenciales NO van en el código. Se leen (en este orden) de:
 *      1) variables de entorno, 2) propiedades -D de la JVM,
 *      3) el archivo ${catalina.base}/conf/jx-notify.properties
 *    Claves: JX_WSP_URL (por defecto http://127.0.0.1:3001/enviar),
 *            JX_WSP_TELEFONO (uno o varios, separados por coma, con código de país sin "+"),
 *            JX_WSP_SECRET (opcional, debe coincidir con WSP_SECRETO del microservicio),
 *            JX_RESEND_API_KEY, JX_MAIL_FROM, JX_MAIL_FROM_NAME, JX_MAIL_TO
 */
public final class Notificador {

    private static final int MAX_INTENTOS = 3;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    private static final ThreadPoolExecutor POOL = crearPool();
    private static final Properties ARCHIVO = cargarArchivo();

    private Notificador() {
    }

    // ------------------------------------------------------------------
    // API pública
    // ------------------------------------------------------------------

    /** Dispara WhatsApp + correo al propietario con los datos del lead recién guardado. */
    public static void notificarLead(Lead lead) {
        try {
            String nombre = txt(lead.getNombreCompleto());
            String telefono = txt(lead.getTelefono());
            String correo = txt(lead.getCorreo());
            String plan = txt(lead.getPlanNombre()).isEmpty() ? "Consulta general" : txt(lead.getPlanNombre());

            String wsp = "🔔 Nuevo Lead Registrado - JumpxFitness\n"
                    + "👤 Nombre: " + nombre + "\n"
                    + "📱 Teléfono: " + telefono + "\n"
                    + "📧 Correo: " + correo + "\n"
                    + "🏷️ Plan de Interés: " + plan;

            String html = plantilla("Nuevo lead registrado", "Alguien pidió información desde la web.",
                    new String[][]{
                        {"Nombre", nombre}, {"Teléfono", telefono}, {"Correo", correo},
                        {"Plan de interés", plan}, {"Fecha", ahora()}},
                    "Puedes responder este correo: llegará directamente al interesado.");

            ejecutar("WhatsApp(lead)", () -> whatsapp(wsp));
            ejecutar("Correo(lead)", () -> correo(cfg("JX_MAIL_TO"), "🔔 Nuevo lead: " + nombre, html, correo, nombre));
        } catch (Exception e) {
            log("notificarLead no pudo preparar los mensajes: " + e);
        }
    }

    /** Aviso al propietario (WhatsApp + correo) y constancia por correo al consumidor. */
    public static void notificarReclamo(Reclamo r, String codigo) {
        try {
            String detalle = recortar(txt(r.getDetalle()), 1200);
            String wsp = "📕 Nuevo " + r.getTipoReclamo().toLowerCase() + " - Libro de Reclamaciones\n"
                    + "🧾 Código: " + codigo + "\n"
                    + "👤 " + r.getNombreCompleto() + " (" + r.getTipoDoc() + " " + r.getNumeroDoc() + ")\n"
                    + "📱 " + r.getTelefono() + "\n"
                    + "📧 " + r.getCorreo() + "\n"
                    + "🏷️ " + r.getTipoBien() + ": " + r.getDescripcionBien() + "\n"
                    + "💰 Monto reclamado: S/ " + r.getMonto().toPlainString() + "\n"
                    + "📝 " + detalle;

            String[][] filas = {
                {"Código", codigo}, {"Tipo", r.getTipoReclamo()}, {"Fecha", ahora()},
                {"Consumidor", r.getNombreCompleto()}, {"Documento", r.getTipoDoc() + " " + r.getNumeroDoc()},
                {"Dirección", r.getDireccion()}, {"Teléfono", r.getTelefono()}, {"Correo", r.getCorreo()},
                {"Apoderado", txt(r.getApoderado()).isEmpty() ? "—" : r.getApoderado()},
                {r.getTipoBien(), r.getDescripcionBien()}, {"Monto reclamado", "S/ " + r.getMonto().toPlainString()},
                {"Detalle", r.getDetalle()}, {"Pedido del consumidor", txt(r.getPedido()).isEmpty() ? "—" : r.getPedido()}};

            String htmlAdmin = plantilla("Nuevo " + r.getTipoReclamo().toLowerCase() + " registrado",
                    "Registrado en el Libro de Reclamaciones virtual.", filas,
                    "Recuerda responder dentro del plazo que fija la normativa vigente.");
            String htmlCliente = plantilla("Constancia de tu " + r.getTipoReclamo().toLowerCase(),
                    "Recibimos tu registro en el Libro de Reclamaciones de JumpxFitness. Guarda este correo como constancia.",
                    filas, "Te responderemos al correo o teléfono que registraste, dentro del plazo legal.");

            ejecutar("WhatsApp(reclamo)", () -> whatsapp(wsp));
            ejecutar("Correo(reclamo-admin)", () -> correo(cfg("JX_MAIL_TO"), "📕 " + r.getTipoReclamo() + " " + codigo, htmlAdmin, r.getCorreo(), r.getNombreCompleto()));
            ejecutar("Correo(reclamo-cliente)", () -> correo(r.getCorreo(), "Constancia de " + r.getTipoReclamo().toLowerCase() + " " + codigo + " - JumpxFitness", htmlCliente, null, null));
        } catch (Exception e) {
            log("notificarReclamo no pudo preparar los mensajes: " + e);
        }
    }

    // ------------------------------------------------------------------
    // Canales
    // ------------------------------------------------------------------

    private static void whatsapp(String mensaje) {
        String url = cfg("JX_WSP_URL");
        if (url.isEmpty()) {
            url = "http://127.0.0.1:3001/enviar";
        }
        String telefonos = cfg("JX_WSP_TELEFONO");
        if (telefonos.isEmpty()) {
            log("WhatsApp no configurado (JX_WSP_TELEFONO): se omite.");
            return;
        }
        String secreto = cfg("JX_WSP_SECRET");

        for (String tel : telefonos.split(",")) {
            final String telefono = tel.trim();
            if (telefono.isEmpty()) {
                continue;
            }
            final String urlFinal = url;
            conReintentos("WhatsApp", () -> {
                JsonObject b = new JsonObject();
                b.addProperty("telefono", telefono);
                b.addProperty("mensaje", mensaje);
                enviarJson(urlFinal, b.toString(), secreto.isEmpty() ? null : "X-Jx-Secret", secreto.isEmpty() ? null : secreto);
            });
        }
    }

    private static void correo(String para, String asunto, String html, String replyTo, String replyNombre) {
        String apiKey = cfg("JX_RESEND_API_KEY");
        String from = cfg("JX_MAIL_FROM");
        if (apiKey.isEmpty() || from.isEmpty() || txt(para).isEmpty()) {
            log("Correo no configurado (JX_RESEND_API_KEY / JX_MAIL_FROM / destinatario): se omite.");
            return;
        }
        String fromName = cfg("JX_MAIL_FROM_NAME").isEmpty() ? "JumpxFitness" : cfg("JX_MAIL_FROM_NAME");

        JsonObject body = new JsonObject();
        body.addProperty("from", fromName + " <" + from + ">");
        JsonArray to = new JsonArray();
        to.add(para.trim());
        body.add("to", to);
        if (replyTo != null && !replyTo.isBlank()) {
            // Resend acepta reply_to como texto plano; no admite un "nombre" aparte como Brevo.
            body.addProperty("reply_to", replyTo.trim());
        }
        body.addProperty("subject", asunto);
        body.addProperty("html", html);
        final String json = body.toString();

        conReintentos("Correo", () -> enviarJson("https://api.resend.com/emails", json, "Authorization", "Bearer " + apiKey));
    }

    // ------------------------------------------------------------------
    // Infraestructura
    // ------------------------------------------------------------------

    private static void enviarJson(String url, String json, String headerNombre, String headerValor) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        if (headerNombre != null) {
            rb.header(headerNombre, headerValor);
        }
        HttpResponse<String> resp = HTTP.send(rb.build(), HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        if (code / 100 == 2) {
            return;
        }
        String msg = "HTTP " + code + " - " + recortar(resp.body(), 300);
        // 4xx (salvo 429) = mal configurado o rechazado: reintentar no sirve.
        if (code / 100 == 4 && code != 429) {
            throw new NoReintentable(msg);
        }
        throw new IOException(msg);
    }

    private static void ejecutar(String nombre, Runnable tarea) {
        try {
            POOL.execute(() -> {
                try {
                    tarea.run();
                } catch (Throwable t) { // un canal jamás debe tumbar al otro
                    log(nombre + " terminó con error: " + t);
                }
            });
        } catch (RejectedExecutionException e) {
            log(nombre + " descartado: cola de notificaciones llena.");
        }
    }

    private static void conReintentos(String canal, TareaIO tarea) {
        for (int i = 1; i <= MAX_INTENTOS; i++) {
            try {
                tarea.ejecutar();
                return;
            } catch (NoReintentable e) {
                log(canal + " rechazado (no se reintenta): " + enmascarar(e.getMessage()));
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log(canal + " intento " + i + "/" + MAX_INTENTOS + " falló: "
                        + e.getClass().getSimpleName() + " " + enmascarar(e.getMessage()));
                if (i < MAX_INTENTOS) {
                    try {
                        Thread.sleep(1500L * i);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    @FunctionalInterface
    private interface TareaIO {
        void ejecutar() throws Exception;
    }

    private static final class NoReintentable extends IOException {
        NoReintentable(String m) {
            super(m);
        }
    }

    private static ThreadPoolExecutor crearPool() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200), r -> {
                    Thread t = new Thread(r, "jx-notificador");
                    t.setDaemon(true);
                    return t;
                });
        p.allowCoreThreadTimeOut(true); // los hilos mueren solos si no hay trabajo
        return p;
    }

    // ------------------------------------------------------------------
    // Configuración y utilidades
    // ------------------------------------------------------------------

    private static Properties cargarArchivo() {
        Properties p = new Properties();
        String base = System.getProperty("catalina.base");
        if (base != null) {
            Path f = Paths.get(base, "conf", "jx-notify.properties");
            if (Files.isReadable(f)) {
                try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                    p.load(r);
                } catch (IOException e) {
                    log("No se pudo leer " + f + ": " + e.getMessage());
                }
            }
        }
        return p;
    }

    private static String cfg(String clave) {
        String v = System.getenv(clave);
        if (v == null || v.isBlank()) {
            v = System.getProperty(clave);
        }
        if (v == null || v.isBlank()) {
            v = ARCHIVO.getProperty(clave);
        }
        return v == null ? "" : v.trim();
    }

    private static String enmascarar(String s) {
        if (s == null) {
            return "";
        }
        for (String clave : new String[]{"JX_RESEND_API_KEY", "JX_WSP_SECRET"}) {
            String secreto = cfg(clave);
            if (!secreto.isEmpty()) {
                s = s.replace(secreto, "***");
            }
        }
        return s;
    }

    private static void log(String m) {
        System.out.println("[Notificador] " + m);
    }

    private static String ahora() {
        return ZonedDateTime.now(LIMA).format(FECHA);
    }

    private static String txt(String s) {
        return s == null ? "" : s.trim();
    }

    private static String recortar(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : (s == null ? "" : s);
    }

    /** Escapa &, <, > y comillas: solo se usa para el HTML del correo. */
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Plantilla HTML responsiva (tablas + estilos en línea, compatible con Gmail/Outlook/móvil). */
    private static String plantilla(String titulo, String subtitulo, String[][] filas, String pie) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
          .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>")
          .append("<body style=\"margin:0;padding:0;background:#fcecef;font-family:Arial,Helvetica,sans-serif;\">")
          .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#fcecef;padding:24px 12px;\"><tr><td align=\"center\">")
          .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:560px;background:#ffffff;border-radius:16px;overflow:hidden;\">")
          .append("<tr><td style=\"background:#d96b73;padding:22px 26px;color:#ffffff;\">")
          .append("<div style=\"font-size:13px;letter-spacing:.12em;text-transform:uppercase;font-weight:bold;\">JumpxFitness</div>")
          .append("<div style=\"font-size:22px;font-weight:bold;margin-top:6px;\">").append(esc(titulo)).append("</div></td></tr>")
          .append("<tr><td style=\"padding:22px 26px 6px;color:#3e3a3b;font-size:15px;line-height:1.5;\">").append(esc(subtitulo)).append("</td></tr>")
          .append("<tr><td style=\"padding:6px 26px 18px;\"><table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
        for (String[] f : filas) {
            sb.append("<tr><td style=\"padding:9px 12px 9px 0;border-bottom:1px solid #eedcdf;color:#7a7072;font-size:13px;width:38%;vertical-align:top;\">")
              .append(esc(f[0]))
              .append("</td><td style=\"padding:9px 0;border-bottom:1px solid #eedcdf;color:#3e3a3b;font-size:14px;vertical-align:top;word-break:break-word;\">")
              .append(esc(f[1]).replace("\n", "<br>")).append("</td></tr>");
        }
        sb.append("</table></td></tr>")
          .append("<tr><td style=\"padding:0 26px 24px;color:#7a7072;font-size:12px;line-height:1.5;\">").append(esc(pie)).append("</td></tr>")
          .append("</table></td></tr></table></body></html>");
        return sb.toString();
    }
}
