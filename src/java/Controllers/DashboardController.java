package Controllers;

import Dao.DashboardDaoImpl;
import Interface.IDashboard;
import Model.Dashboard;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "DashboardController", urlPatterns = {"/DashboardController"})
public class DashboardController extends HttpServlet {

    private final IDashboard dashboardDao = new DashboardDaoImpl();
    private final Gson gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.util.Date.class,
                    (com.google.gson.JsonSerializer<java.util.Date>) (date, type, ctx)
                    -> new com.google.gson.JsonPrimitive(
                            new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date)))
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== DASHBOARD CONTROLLER ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {
            if ("resumen".equals(action)) {
                Dashboard data = dashboardDao.getResumenGeneral();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", gson.toJsonTree(data));
                out.print(jsonResponse.toString());

            } else if ("kpis".equals(action)) {
                Dashboard data = dashboardDao.getResumenGeneral();
                JsonObject kpis = new JsonObject();
                kpis.addProperty("totalClientes", data.getTotalClientes());
                kpis.addProperty("reservasHoy", data.getReservasHoy());
                kpis.addProperty("ventasHoy", data.getVentasHoy());
                kpis.addProperty("sesionesJumpingHoy", data.getSesionesJumpingHoy());
                kpis.addProperty("ventasVariacion", data.getVentasVariacion());
                kpis.addProperty("clientesVariacion", data.getClientesVariacion());
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", kpis);
                out.print(jsonResponse.toString());

            } else if ("reservasHoy".equals(action)) {
                var reservas = dashboardDao.getReservasHoy();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", gson.toJsonTree(reservas));
                out.print(jsonResponse.toString());

            } else if ("sesionesHoy".equals(action)) {
                var sesiones = dashboardDao.getSesionesJumpingHoy();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", gson.toJsonTree(sesiones));
                out.print(jsonResponse.toString());

            } else if ("stockBajo".equals(action)) {
                var productos = dashboardDao.getProductosStockBajo();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", gson.toJsonTree(productos));
                out.print(jsonResponse.toString());

            } else if ("ventasSemana".equals(action)) {
                var ventas = dashboardDao.getVentasProductosSemana();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", gson.toJsonTree(ventas));
                out.print(jsonResponse.toString());

            } else if ("reservasSemana".equals(action)) {
                var reservas = dashboardDao.getReservasCanchaSemana();
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", gson.toJsonTree(reservas));
                out.print(jsonResponse.toString());
                
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Acción no válida: " + action);
                out.print(jsonResponse.toString());
            }
        } catch (Exception e) {
            response.setStatus(500);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
            response.getWriter().print(jsonResponse.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
