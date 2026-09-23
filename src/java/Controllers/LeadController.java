package Controllers;

import Dao.LeadDaoImpl;
import Dao.PlanJumpingDaoImpl;
import Model.PlanJumping;
import Model.Lead;
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
import java.util.regex.Pattern;

@WebServlet(name = "LeadController", urlPatterns = {"/LeadController"})
public class LeadController extends HttpServlet {

    private final LeadDaoImpl leadDao = new LeadDaoImpl();
    private final PlanJumpingDaoImpl planDao = new PlanJumpingDaoImpl();
    private final Gson gson = new Gson();

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    // Patrón válido tanto en Java como en HTML (sin escape de guion)
    private static final Pattern PHONE = Pattern.compile("^[0-9+() \\-]{7,20}$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject out = new JsonObject();

        try (PrintWriter writer = response.getWriter()) {

            // Máximo 8 solicitudes cada 10 minutos por IP: evita que el formulario público
            // se use para inundar de spam la tabla de leads y las notificaciones del dueño.
            String ip = clienteIp(request);
            if (!RateLimiter.permitir("lead:" + ip, 8, 10 * 60 * 1000L)) {
                error(response, writer, out, 429, "Has enviado demasiadas solicitudes. Espera unos minutos e inténtalo de nuevo.");
                return;
            }

            JsonObject body = leerJson(request);

            String nombre = limpiar(body, "nombreCompleto");
            String correo = limpiar(body, "correo").toLowerCase();
            String telefono = limpiar(body, "telefono");
            String planNombre = limpiar(body, "planNombre");
            Integer idPlan = null;

            if (body.has("idPlan") && !body.get("idPlan").isJsonNull()) {
                try {
                    idPlan = body.get("idPlan").getAsInt();
                } catch (Exception ignored) {
                }
            }

            // Validaciones
            if (nombre.length() < 3 || nombre.length() > 120) {
                error(response, writer, out, 400, "Ingresa tu nombre completo.");
                return;
            }
            if (!EMAIL.matcher(correo).matches() || correo.length() > 150) {
                error(response, writer, out, 400, "Ingresa un correo electrónico válido.");
                return;
            }
            if (!PHONE.matcher(telefono).matches()) {
                error(response, writer, out, 400, "Ingresa un número de teléfono válido.");
                return;
            }

            // Validación del plan (solo si viene idPlan)
            if (idPlan != null) {
                if (idPlan <= 0) {
                    error(response, writer, out, 400, "El plan seleccionado no es válido.");
                    return;
                }
                try {
                    PlanJumping plan = planDao.SearchById(idPlan);
                    if (plan == null || !plan.isActivo()) {
                        error(response, writer, out, 400, "El plan seleccionado ya no está disponible.");
                        return;
                    }
                    planNombre = plan.getNombre();
                } catch (Exception ex) {
                    System.err.println(">>> Error al buscar plan id=" + idPlan + " <<<");
                    ex.printStackTrace();
                    error(response, writer, out, 500, "No se pudo validar el plan seleccionado.");
                    return;
                }
            }

            if (planNombre.length() > 100) {
                planNombre = planNombre.substring(0, 100);
            }

            Lead lead = new Lead();
            lead.setNombreCompleto(nombre);
            lead.setCorreo(correo);
            lead.setTelefono(telefono);
            lead.setIdPlan(idPlan);
            lead.setPlanNombre(planNombre);

            boolean ok = leadDao.insertar(lead);
            if (!ok) {
                error(response, writer, out, 500, "No se pudo registrar tu solicitud. Inténtalo nuevamente.");
                return;
            }

            // El lead YA quedó guardado en Oracle. Lo que sigue es asíncrono (Telegram + correo)
            // y corre en Notificador: si falla, se reintenta solo y NUNCA revierte este insert
            // ni afecta la respuesta que recibe el usuario.
            Notificador.notificarLead(lead);

            response.setStatus(HttpServletResponse.SC_OK);
            out.addProperty("success", true);
            out.addProperty("message", "¡Listo! Recibimos tus datos y pronto nos pondremos en contacto contigo.");
            out.addProperty("idLead", lead.getIdLead());
            writer.print(out);

        } catch (Exception e) {
            System.err.println(">>> ERROR GENERAL en LeadController <<<");
            System.err.println("Tipo: " + e.getClass().getName());
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();

            response.setStatus(500);
            out.addProperty("success", false);
            out.addProperty("message", "Ocurrió un error al procesar la solicitud.");
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
