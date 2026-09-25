package Controllers;

import Dao.ClienteDaoImpl;
import Dao.PagoCulqiDaoImpl;
import Dao.PlanJumpingDaoImpl;
import Model.Cliente;
import Model.PagoCulqi;
import Model.PlanJumping;
import Model.Rol;
import Model.Usuario;
import Util.AuditoriaHelper;
import Util.CulqiClient;
import Util.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Pagos con Culqi: tarjeta y Yape se cobran al toque contra /v2/charges
 * (mismo endpoint para ambos, solo cambia el token que genera CulqiJS en el
 * navegador). PagoEfectivo genera un CIP vía /v2/orders y se confirma
 * DESPUÉS, cuando Culqi llama a nuestro webhook (acción "webhook").
 *
 * REQUISITOS que no dependen de este código:
 *  - Cuenta de comercio en Culqi (culqi.com) y llaves de prueba/producción
 *    (CulqiPanel > Desarrollo > API Keys). La llave PÚBLICA (pk_) va en el
 *    frontend (no es secreta); la llave SECRETA (sk_) va solo en el
 *    servidor, en JX_CULQI_SECRET_KEY (env var o conf/jx-notify.properties).
 *  - Para que el webhook de PagoEfectivo funcione, este servidor debe tener
 *    una URL pública HTTPS accesible desde internet (Culqi les pega desde
 *    afuera). En localhost/NetBeans esto NO llega solo; hace falta desplegar
 *    en un hosting real o exponer temporalmente con algo como ngrok, y
 *    registrar esa URL en CulqiPanel > Eventos > Webhooks con el evento
 *    "order.status.changed".
 */
@WebServlet(name = "PagoCulqiController", urlPatterns = {"/PagoCulqiController"})
public class PagoCulqiController extends HttpServlet {

    private final PagoCulqiDaoImpl pagoDao = new PagoCulqiDaoImpl();
    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final PlanJumpingDaoImpl planDao = new PlanJumpingDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        JsonObject out = new JsonObject();

        try (PrintWriter writer = response.getWriter()) {
            Usuario sesion = usuarioDeSesion(request);

            if ("misPagos".equals(action)) {
                if (sesion == null || sesion.getRol() != Rol.CLIENTE) { noAutorizado(response, writer, out); return; }
                Cliente cliente = clienteDao.SearchByPersonaId(sesion.getPersona().getId_persona());
                var lista = cliente == null ? java.util.List.<PagoCulqi>of() : pagoDao.listaPorCliente(cliente.getId_cliente());
                out.addProperty("success", true);
                out.add("data", gson.toJsonTree(lista));
                writer.print(out);

            } else if ("historial".equals(action)) {
                if (sesion == null || sesion.getRol() != Rol.ADMIN) { noAutorizado(response, writer, out); return; }
                out.addProperty("success", true);
                out.add("data", gson.toJsonTree(pagoDao.listaTodos()));
                writer.print(out);

            } else if ("estadoOrden".equals(action)) {
                // El cliente hace polling de esto tras generar un CIP, para saber cuándo se confirmó.
                if (sesion == null || sesion.getRol() != Rol.CLIENTE) { noAutorizado(response, writer, out); return; }
                int idPago = parseIntSeguro(request.getParameter("idPago"));
                PagoCulqi pago = pagoDao.SearchById(idPago);
                out.addProperty("success", true);
                out.addProperty("estado", pago == null ? "DESCONOCIDO" : pago.getEstado());
                writer.print(out);

            } else {
                out.addProperty("success", false);
                out.addProperty("message", "Acción no válida");
                writer.print(out);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        JsonObject out = new JsonObject();

        try (PrintWriter writer = response.getWriter()) {

            if ("webhook".equals(action)) {
                manejarWebhook(request, response, writer, out);
                return;
            }

            Usuario sesion = usuarioDeSesion(request);
            if (sesion == null || sesion.getRol() != Rol.CLIENTE) { noAutorizado(response, writer, out); return; }

            if (!CulqiClient.configurado()) {
                out.addProperty("success", false);
                out.addProperty("message", "Los pagos en línea aún no están configurados (falta JX_CULQI_SECRET_KEY).");
                writer.print(out);
                return;
            }

            Cliente cliente = clienteDao.SearchByPersonaId(sesion.getPersona().getId_persona());
            if (cliente == null) {
                out.addProperty("success", false);
                out.addProperty("message", "Tu cuenta no tiene un perfil de cliente asociado.");
                writer.print(out);
                return;
            }

            int idPlan = parseIntSeguro(request.getParameter("idPlan"));
            PlanJumping plan = idPlan > 0 ? planDao.SearchById(idPlan) : null;
            if (plan == null || !plan.isActivo()) {
                out.addProperty("success", false);
                out.addProperty("message", "El plan seleccionado no está disponible.");
                writer.print(out);
                return;
            }

            String correo = limpiar(request, "email");
            if (correo.isEmpty()) correo = sesion.getUsuario() + "@jumpxfitness.local";
            int montoCentimos = (int) Math.round(plan.getPrecio() * 100);

            if ("cargoTarjeta".equals(action) || "cargoYape".equals(action)) {
                if (!RateLimiter.permitir("culqi-cargo:" + sesion.getId_usuario(), 8, 15 * 60 * 1000L)) {
                    response.setStatus(429);
                    out.addProperty("success", false);
                    out.addProperty("message", "Demasiados intentos de pago. Espera unos minutos.");
                    writer.print(out);
                    return;
                }
                String sourceId = limpiar(request, "sourceId");
                if (sourceId.isEmpty()) {
                    out.addProperty("success", false);
                    out.addProperty("message", "No se generó el token de pago. Vuelve a intentarlo.");
                    writer.print(out);
                    return;
                }
                String metodo = "cargoYape".equals(action) ? "YAPE" : "TARJETA";

                PagoCulqi pago = new PagoCulqi();
                pago.setCliente(cliente);
                pago.setPlan(plan);
                pago.setMetodo(metodo);
                pago.setMonto(plan.getPrecio());
                pago.setMoneda("PEN");
                pago.setEstado("PENDIENTE");
                pagoDao.crear(pago);

                try {
                    JsonObject cargo = CulqiClient.crearCargo(montoCentimos, "PEN",
                            "Plan " + plan.getNombre() + " - JumpxFitness", correo, sourceId);
                    String idCargo = cargo.has("id") ? cargo.get("id").getAsString() : null;
                    pagoDao.actualizarPorCargo(idCargo, "PENDIENTE", cargo.toString());
                    boolean ok = pagoDao.marcarConfirmadoYExtenderPlan(pago.getIdPago());

                    AuditoriaHelper.registrarConUsuario(request, sesion.getId_usuario(), sesion.getUsuario(),
                            "PAGO", "pago_culqi_jx", pago.getIdPago(),
                            "Pago con " + metodo + " por el plan '" + plan.getNombre() + "' (Culqi charge " + idCargo + ")");

                    out.addProperty("success", ok);
                    out.addProperty("message", ok ? "¡Pago exitoso! Tu plan ya está activo." : "El pago se procesó pero no se pudo activar el plan; contáctanos.");
                    writer.print(out);

                } catch (CulqiClient.CulqiException ce) {
                    pagoDao.actualizarPorCargo(null, "RECHAZADO", ce.respuesta.toString());
                    out.addProperty("success", false);
                    out.addProperty("message", "Tu pago fue rechazado: " + ce.getMessage());
                    writer.print(out);
                }

            } else if ("registrarOrdenPagoEfectivo".equals(action)) {
                // Flujo recomendado: el Checkout de Culqi ya creó la orden (CIP) en el
                // navegador del cliente; aquí solo la asociamos a un pago nuestro para
                // poder marcarlo como pagado cuando llegue el webhook.
                String culqiOrderId = limpiar(request, "culqiOrderId");
                if (culqiOrderId.isEmpty()) {
                    out.addProperty("success", false);
                    out.addProperty("message", "No se recibió el código de la orden generada por Culqi.");
                    writer.print(out);
                    return;
                }
                PagoCulqi pago = new PagoCulqi();
                pago.setCliente(cliente);
                pago.setPlan(plan);
                pago.setMetodo("PAGOEFECTIVO");
                pago.setMonto(plan.getPrecio());
                pago.setMoneda("PEN");
                pago.setEstado("PENDIENTE");
                pago.setCulqiOrderId(culqiOrderId);
                boolean ok = pagoDao.crear(pago);

                out.addProperty("success", ok);
                out.addProperty("message", ok
                        ? "Listo. En cuanto Culqi confirme tu pago, tu plan se activará solo."
                        : "No se pudo registrar tu pago. Contáctanos con tu código de operación.");
                writer.print(out);

            } else if ("ordenPagoEfectivo".equals(action)) {
                // Alternativa avanzada: generar el CIP nosotros mismos por API en vez
                // de usar el Checkout emergente (útil si luego se arma un formulario
                // propio). No usada por cliente_dashboard.js por defecto.
                if (!RateLimiter.permitir("culqi-orden:" + sesion.getId_usuario(), 5, 30 * 60 * 1000L)) {
                    response.setStatus(429);
                    out.addProperty("success", false);
                    out.addProperty("message", "Demasiadas solicitudes. Espera unos minutos.");
                    writer.print(out);
                    return;
                }

                PagoCulqi pago = new PagoCulqi();
                pago.setCliente(cliente);
                pago.setPlan(plan);
                pago.setMetodo("PAGOEFECTIVO");
                pago.setMonto(plan.getPrecio());
                pago.setMoneda("PEN");
                pago.setEstado("PENDIENTE");
                pagoDao.crear(pago);

                try {
                    String nombre = sesion.getPersona() != null ? sesion.getPersona().getNombre() : sesion.getUsuario();
                    String apellido = sesion.getPersona() != null ? sesion.getPersona().getApellido() : "";
                    String telefono = sesion.getPersona() != null ? sesion.getPersona().getTelefono() : "";

                    JsonObject orden = CulqiClient.crearOrden(montoCentimos, "PEN",
                            "Plan " + plan.getNombre() + " - JumpxFitness",
                            "jx-" + pago.getIdPago(), nombre, apellido, correo, telefono, 24 * 60 * 60);
                    String idOrden = orden.has("id") ? orden.get("id").getAsString() : null;
                    pagoDao.actualizarPorOrden(idOrden, "PENDIENTE", orden.toString());

                    out.addProperty("success", true);
                    out.addProperty("message", "Genera tu pago con el código que te mostramos. Se confirmará solo apenas lo pagues.");
                    out.addProperty("idPago", pago.getIdPago());
                    // Devolvemos el JSON completo de Culqi: no estamos 100% seguros del nombre
                    // exacto del campo del CIP/URL sin haberlo probado contra una cuenta real.
                    out.add("ordenCulqi", orden);
                    writer.print(out);

                } catch (CulqiClient.CulqiException ce) {
                    pagoDao.actualizarPorOrden(null, "RECHAZADO", ce.respuesta.toString());
                    out.addProperty("success", false);
                    out.addProperty("message", "No se pudo generar el código de pago: " + ce.getMessage());
                    writer.print(out);
                }

            } else {
                out.addProperty("success", false);
                out.addProperty("message", "Acción no válida");
                writer.print(out);
            }

        } catch (Exception e) {
            response.setStatus(500);
            System.err.println(">>> ERROR GENERAL en PagoCulqiController <<<");
            e.printStackTrace();
            out.addProperty("success", false);
            out.addProperty("message", "Ocurrió un error al procesar el pago.");
            try {
                response.getWriter().print(gson.toJson(out));
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Webhook de Culqi (evento order.status.changed). Público, sin sesión:
     * lo llama el servidor de Culqi, no el navegador del cliente.
     * Por seguridad NUNCA confiamos en el estado que venga en el body: se
     * vuelve a pedir la orden completa a la API de Culqi con nuestra propia
     * llave secreta antes de dar el plan por pagado.
     */
    private void manejarWebhook(HttpServletRequest request, HttpServletResponse response,
                                 PrintWriter writer, JsonObject out) {
        try {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }
            JsonObject evento = body.length() == 0 ? new JsonObject() : JsonParser.parseString(body.toString()).getAsJsonObject();
            String idOrden = evento.has("id") ? evento.get("id").getAsString()
                    : evento.has("data") && evento.get("data").isJsonObject() && evento.getAsJsonObject("data").has("id")
                        ? evento.getAsJsonObject("data").get("id").getAsString() : null;

            if (idOrden == null) {
                System.out.println("[PagoCulqiController] Webhook sin id de orden reconocible: " + body);
                response.setStatus(200); // respondemos 200 igual para que Culqi no siga reintentando
                out.addProperty("success", true);
                writer.print(out);
                return;
            }

            JsonObject ordenReal = CulqiClient.obtenerOrden(idOrden);
            String estadoCrudo = extraerEstadoOrden(ordenReal);

            PagoCulqi pago = pagoDao.SearchByOrderId(idOrden);
            if (pago == null) {
                System.out.println("[PagoCulqiController] Webhook para una orden que no tengo registrada: " + idOrden);
                response.setStatus(200);
                out.addProperty("success", true);
                writer.print(out);
                return;
            }

            if (esPagado(estadoCrudo)) {
                pagoDao.actualizarPorOrden(idOrden, "PENDIENTE", ordenReal.toString()); // guarda el JSON fresco
                boolean ok = pagoDao.marcarConfirmadoYExtenderPlan(pago.getIdPago());
                System.out.println("[PagoCulqiController] Orden " + idOrden + " pagada. Plan extendido: " + ok);
            } else if (esExpirado(estadoCrudo)) {
                pagoDao.actualizarPorOrden(idOrden, "EXPIRADO", ordenReal.toString());
            } else {
                pagoDao.actualizarPorOrden(idOrden, "PENDIENTE", ordenReal.toString());
            }

            response.setStatus(200);
            out.addProperty("success", true);
            writer.print(out);

        } catch (Exception e) {
            System.err.println(">>> ERROR procesando webhook de Culqi <<<");
            e.printStackTrace();
            // 200 igual: si respondemos error, Culqi seguirá reintentando el mismo webhook indefinidamente.
            response.setStatus(200);
            out.addProperty("success", false);
            writer.print(out);
        }
    }

    /**
     * El nombre y los valores exactos del campo de estado de una orden no
     * están confirmados sin una cuenta de prueba real; probamos varios
     * nombres típicos. Ajusta esto con el JSON real que veas en tu primera
     * prueba (te lo dejamos completo en pago_culqi_jx.respuesta_json).
     */
    private String extraerEstadoOrden(JsonObject orden) {
        for (String campo : new String[]{"state", "status", "estado"}) {
            if (orden.has(campo) && !orden.get(campo).isJsonNull()) {
                return orden.get(campo).getAsString().toLowerCase();
            }
        }
        return "";
    }

    private boolean esPagado(String estado) {
        return estado.contains("pag") || estado.contains("paid") || estado.contains("complet") || estado.contains("captur");
    }

    private boolean esExpirado(String estado) {
        return estado.contains("expir") || estado.contains("cancel") || estado.contains("rechaz") || estado.contains("declin");
    }

    private Usuario usuarioDeSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Usuario) session.getAttribute("usuario");
    }

    private void noAutorizado(HttpServletResponse response, PrintWriter writer, JsonObject out) {
        response.setStatus(403);
        out.addProperty("success", false);
        out.addProperty("message", "No tienes permiso para realizar esta acción.");
        writer.print(out);
    }

    private int parseIntSeguro(String v) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return -1; }
    }

    private String limpiar(HttpServletRequest request, String param) {
        String v = request.getParameter(param);
        return v == null ? "" : v.trim().replaceAll("[\\u0000-\\u001F]", "");
    }
}
