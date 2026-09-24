package Controllers;

import Dao.ClienteDaoImpl;
import Dao.RegistroJumpingDaoImpl;
import Model.Cliente;
import Model.RegistroJumping;
import Model.Rol;
import Model.Usuario;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Resumen del plan vigente para el dashboard del cliente: qué plan tiene,
 * desde cuándo y cuántos días de vigencia le quedan. La vigencia se calcula
 * a partir de su registrojumping más reciente (fecha_ingreso + diasVigencia
 * del plan); no existe una tabla de "suscripción" aparte.
 */
@WebServlet(name = "MiPlanController", urlPatterns = {"/MiPlanController"})
public class MiPlanController extends HttpServlet {

    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final RegistroJumpingDaoImpl registroDao = new RegistroJumpingDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject out = new JsonObject();

        try (PrintWriter writer = response.getWriter()) {
            HttpSession session = request.getSession(false);
            Usuario sesion = session == null ? null : (Usuario) session.getAttribute("usuario");

            if (sesion == null || sesion.getRol() != Rol.CLIENTE) {
                response.setStatus(403);
                out.addProperty("success", false);
                out.addProperty("message", "No tienes permiso para ver esta información.");
                writer.print(out);
                return;
            }

            Cliente cliente = clienteDao.SearchByPersonaId(sesion.getPersona().getId_persona());
            if (cliente == null) {
                out.addProperty("success", true);
                out.addProperty("tienePlan", false);
                out.addProperty("message", "Tu cuenta aún no tiene un perfil de cliente asociado.");
                writer.print(out);
                return;
            }

            List<RegistroJumping> registros = registroDao.SearchByClienteId(cliente.getId_cliente());
            if (registros == null || registros.isEmpty()) {
                out.addProperty("success", true);
                out.addProperty("tienePlan", false);
                writer.print(out);
                return;
            }

            // SearchByClienteId ya viene ordenado por fecha_ingreso DESC: el primero es el más reciente.
            RegistroJumping ultimo = registros.get(0);
            LocalDate inicio = ultimo.getFechaIngreso().toLocalDate();
            int diasVigencia = ultimo.getPlan().getDiasVigencia();
            LocalDate vence = inicio.plusDays(diasVigencia);
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), vence);

            out.addProperty("success", true);
            out.addProperty("tienePlan", true);
            out.addProperty("nombrePlan", ultimo.getPlan().getNombre());
            out.addProperty("precioPlan", ultimo.getPlan().getPrecio());
            out.addProperty("fechaInicio", inicio.toString());
            out.addProperty("fechaVencimiento", vence.toString());
            out.addProperty("diasRestantes", diasRestantes);
            out.addProperty("vencido", diasRestantes < 0);
            writer.print(out);

        } catch (Exception e) {
            response.setStatus(500);
            System.err.println(">>> ERROR GENERAL en MiPlanController <<<");
            e.printStackTrace();
            out.addProperty("success", false);
            out.addProperty("message", "Ocurrió un error al obtener tu plan.");
            try {
                response.getWriter().print(out.toString());
            } catch (IOException ignored) {
            }
        }
    }
}
