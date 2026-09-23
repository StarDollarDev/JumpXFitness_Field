package Controllers;

import Dao.ReclamoDaoImpl;
import Model.Reclamo;
import Util.Notificador;
import Util.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Libro de Reclamaciones virtual (formulario público, sin login).
 * Guarda en Oracle (reclamo_jx) y SIEMPRE responde éxito al consumidor si
 * el guardado fue correcto, aunque Telegram/correo fallen (se disparan
 * de forma asíncrona en Notificador y nunca revierten la inserción).
 */
@WebServlet(name = "ReclamoController", urlPatterns = {"/ReclamoController"})
public class ReclamoController extends HttpServlet {

    private final ReclamoDaoImpl reclamoDao = new ReclamoDaoImpl();
    private final Gson gson = new Gson();

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+() \\-]{7,20}$");
    private static final Pattern DOC = Pattern.compile("^[0-9A-Za-z\\-]{6,15}$");
    private static final Set<String> TIPO_DOC = Set.of("DNI", "CE");
    private static final Set<String> TIPO_BIEN = Set.of("PRODUCTO", "SERVICIO");
    private static final Set<String> TIPO_RECLAMO = Set.of("RECLAMO", "QUEJA");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject out = new JsonObject();

        try (PrintWriter writer = response.getWriter()) {

            // Máximo 5 reclamos cada 10 minutos por IP: evita spam sin estorbar a un consumidor real
            String ip = clienteIp(request);
            if (!RateLimiter.permitir("reclamo:" + ip, 5, 10 * 60 * 1000L)) {
                error(response, writer, out, 429, "Has enviado demasiadas solicitudes. Espera unos minutos e inténtalo de nuevo.");
                return;
            }

            JsonObject body = leerJson(request);

            String nombre = limpiar(body, "nombreCompleto");
            String tipoDoc = limpiar(body, "tipoDoc").toUpperCase();
            String numeroDoc = limpiar(body, "numeroDoc");
            String direccion = limpiar(body, "direccion");
            String telefono = limpiar(body, "telefono");
            String correo = limpiar(body, "correo").toLowerCase();
            String apoderado = limpiar(body, "apoderado");
            String tipoBien = limpiar(body, "tipoBien").toUpperCase();
            String descripcionBien = limpiar(body, "descripcionBien");
            String montoTxt = limpiar(body, "monto");
            String tipoReclamo = limpiar(body, "tipoReclamo").toUpperCase();
            String detalle = limpiar(body, "detalle");
            String pedido = limpiar(body, "pedido");
            boolean aceptaTerminos = body.has("aceptaTerminos") && !body.get("aceptaTerminos").isJsonNull()
                    && body.get("aceptaTerminos").getAsBoolean();

            // --- Validaciones (obligatorias según Indecopi) ---
            if (nombre.length() < 3 || nombre.length() > 150) {
                error(response, writer, out, 400, "Ingresa el nombre completo del consumidor.");
                return;
            }
            if (!TIPO_DOC.contains(tipoDoc)) {
                error(response, writer, out, 400, "Selecciona un tipo de documento válido (DNI o CE).");
                return;
            }
            if (!DOC.matcher(numeroDoc).matches()) {
                error(response, writer, out, 400, "Ingresa un número de documento válido.");
                return;
            }
            if (direccion.length() < 5 || direccion.length() > 200) {
                error(response, writer, out, 400, "Ingresa tu dirección.");
                return;
            }
            if (!PHONE.matcher(telefono).matches()) {
                error(response, writer, out, 400, "Ingresa un número de teléfono válido.");
                return;
            }
            if (!EMAIL.matcher(correo).matches() || correo.length() > 150) {
                error(response, writer, out, 400, "Ingresa un correo electrónico válido.");
                return;
            }
            if (!TIPO_BIEN.contains(tipoBien)) {
                error(response, writer, out, 400, "Indica si el reclamo es sobre un producto o un servicio.");
                return;
            }
            if (descripcionBien.length() < 3 || descripcionBien.length() > 200) {
                error(response, writer, out, 400, "Describe brevemente el producto o servicio.");
                return;
            }
            BigDecimal monto;
            try {
                monto = new BigDecimal(montoTxt.replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
                if (monto.signum() < 0 || monto.compareTo(new BigDecimal("999999.99")) > 0) {
                    throw new NumberFormatException();
                }
            } catch (Exception ex) {
                error(response, writer, out, 400, "Ingresa un monto reclamado válido.");
                return;
            }
            if (!TIPO_RECLAMO.contains(tipoReclamo)) {
                error(response, writer, out, 400, "Indica si es un reclamo o una queja.");
                return;
            }
            if (detalle.length() < 10 || detalle.length() > 2000) {
                error(response, writer, out, 400, "Describe el detalle del reclamo (mínimo 10 caracteres).");
                return;
            }
            if (pedido.length() > 500) {
                pedido = pedido.substring(0, 500);
            }
            if (apoderado.length() > 150) {
                apoderado = apoderado.substring(0, 150);
            }
            if (!aceptaTerminos) {
                error(response, writer, out, 400, "Debes aceptar los términos del registro para continuar.");
                return;
            }

            Reclamo r = new Reclamo();
            r.setNombreCompleto(nombre);
            r.setTipoDoc(tipoDoc);
            r.setNumeroDoc(numeroDoc);
            r.setDireccion(direccion);
            r.setTelefono(telefono);
            r.setCorreo(correo);
            r.setApoderado(apoderado);
            r.setTipoBien(tipoBien);
            r.setDescripcionBien(descripcionBien);
            r.setMonto(monto);
            r.setTipoReclamo(tipoReclamo);
            r.setDetalle(detalle);
            r.setPedido(pedido);

            boolean ok = reclamoDao.insertar(r);
            if (!ok) {
                error(response, writer, out, 500, "No se pudo registrar tu reclamo. Inténtalo nuevamente.");
                return;
            }

            // La inserción en Oracle ya está confirmada: lo demás es asíncrono y no la afecta.
            Notificador.notificarReclamo(r, r.getCodigo());

            response.setStatus(HttpServletResponse.SC_OK);
            out.addProperty("success", true);
            out.addProperty("message", "Tu " + (tipoReclamo.equals("QUEJA") ? "queja" : "reclamo")
                    + " fue registrado. Código: " + r.getCodigo() + ". Te enviaremos una constancia a tu correo.");
            out.addProperty("codigo", r.getCodigo());
            writer.print(out);

        } catch (Exception e) {
            System.err.println(">>> ERROR GENERAL en ReclamoController <<<");
            e.printStackTrace();
            response.setStatus(500);
            out.addProperty("success", false);
            out.addProperty("message", "Ocurrió un error al procesar tu reclamo.");
            try {
                response.getWriter().print(gson.toJson(out));
            } catch (IOException ignored) {
            }
        }
    }

    private JsonObject leerJson(HttpServletRequest request) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        if (json.length() == 0) {
            return new JsonObject();
        }
        return JsonParser.parseString(json.toString()).getAsJsonObject();
    }

    private String limpiar(JsonObject body, String key) {
        if (!body.has(key) || body.get(key).isJsonNull()) {
            return "";
        }
        return body.get(key).getAsString().trim().replaceAll("[\\u0000-\\u001F]", "");
    }

    private String clienteIp(HttpServletRequest request) {
        String fwd = request.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            return fwd.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void error(HttpServletResponse response, PrintWriter writer, JsonObject out,
                        int status, String message) {
        response.setStatus(status);
        out.addProperty("success", false);
        out.addProperty("message", message);
        writer.print(out);
    }
}
