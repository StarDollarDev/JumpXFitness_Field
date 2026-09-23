package Controllers;

import Dao.AuditoriaDaoImpl;
import Interface.IAuditoria;
import Model.Auditoria;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/** Solo lectura: la auditoría se escribe únicamente vía AuditoriaHelper. */
@WebServlet(name = "AuditoriaController", urlPatterns = {"/AuditoriaController"})
public class AuditoriaController extends HttpServlet {

    private final IAuditoria aDao = new AuditoriaDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        try (PrintWriter out = response.getWriter()) {
            List<Auditoria> lista;

            if ("listar".equals(action)) {
                lista = aDao.lista();
            } else if ("listarPorTabla".equals(action)) {
                lista = aDao.listarPorTabla(request.getParameter("tabla"));
            } else if ("listarPorUsuario".equals(action)) {
                lista = aDao.listarPorUsuario(Integer.parseInt(request.getParameter("idUsuario")));
            } else if ("listarPorAccion".equals(action)) {
                lista = aDao.listarPorAccion(request.getParameter("accion"));
            } else if ("listarPorRango".equals(action)) {
                lista = aDao.listarPorRangoFecha(request.getParameter("inicio"), request.getParameter("fin"));
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Acción no válida: " + action);
                out.print(jsonResponse.toString());
                return;
            }

            JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();
            jsonResponse.addProperty("success", true);
            jsonResponse.add("data", jsonArray);
            out.print(jsonResponse.toString());
        } catch (Exception e) {
            response.setStatus(500);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
            response.getWriter().print(jsonResponse.toString());
        }
    }
}
