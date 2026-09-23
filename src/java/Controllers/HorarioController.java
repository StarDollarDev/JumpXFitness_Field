package Controllers;

import Dao.HorarioDaoImpl;
import Interface.IHorario;
import Model.Horario;
import Util.AuditoriaHelper;
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

@WebServlet(name = "HorarioController", urlPatterns = {"/HorarioController"})
public class HorarioController extends HttpServlet {

    private final IHorario hDao = new HorarioDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== HORARIO JUMPING CONTROLLER (GET) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {
            if ("listar".equals(action)) {
                // Listar TODOS los horarios de Jumping
                List<Horario> lista = hDao.lista();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Horarios de Jumping listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());

            } else if ("listarActivos".equals(action)) {
                List<Horario> lista = hDao.listaActivos();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Horarios de Jumping activos listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());

            } else if ("buscar".equals(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    Horario h = hDao.SearchById(id);
                    if (h != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(h));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Horario de Jumping no encontrado");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID requerido");
                }
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
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== HORARIO JUMPING CONTROLLER (POST) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {
            if ("insertar".equals(action)) {

                String horaInicio = request.getParameter("hora_inicio");
                String horaFin = request.getParameter("hora_fin");
                String activoStr = request.getParameter("activo");

                if (horaInicio != null && !horaInicio.trim().isEmpty()
                        && horaFin != null && !horaFin.trim().isEmpty()) {
                    if (!validarHora(horaInicio) || !validarHora(horaFin)) {

                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Formato de hora inválido. Use HH:mm");
                        out.print(jsonResponse.toString());
                        return;
                    }

                    Horario h = new Horario();
                    h.setHora_inicio(horaInicio + ":00");
                    h.setHora_fin(horaFin + ":00");
                    h.setActivo(activoStr != null ? Boolean.parseBoolean(activoStr) : true);

                    boolean resultado = hDao.insertar(h);

                    jsonResponse.addProperty("success", resultado);

                    if (resultado) {
                        jsonResponse.addProperty("message", "Horario de Jumping insertado correctamente");
                        jsonResponse.addProperty("id_horario", h.getId_horario());
                        jsonResponse.add("data", gson.toJsonTree(h));
                        AuditoriaHelper.registrar(request, "INSERT", "horario", h.getId_horario(),
                                "Alta de horario " + h.getHora_inicio() + " - " + h.getHora_fin());
                    } else {
                        jsonResponse.addProperty("message", "Error al insertar horario de Jumping");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Hora de inicio y hora fin son requeridas");
                }
                out.print(jsonResponse.toString());

            } else if ("actualizar".equals(action)) {

                String idParam = request.getParameter("id_horario");
                String horaInicio = request.getParameter("hora_inicio");
                String horaFin = request.getParameter("hora_fin");
                String activoStr = request.getParameter("activo");

                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    Horario h = hDao.SearchById(id);

                    if (h != null) {
                        if (horaInicio != null && !horaInicio.trim().isEmpty()) {

                            if (!validarHora(horaInicio)) {

                                jsonResponse.addProperty("success", false);
                                jsonResponse.addProperty("message", "Formato de hora inválido. Use HH:mm");
                                out.print(jsonResponse.toString());
                                return;
                            }
                            h.setHora_inicio(horaInicio + ":00");
                        }
                        if (horaFin != null && !horaFin.trim().isEmpty()) {
                            if (!validarHora(horaFin)) {
                                jsonResponse.addProperty("success", false);
                                jsonResponse.addProperty("message", "Formato de hora inválido. Use HH:mm");
                                out.print(jsonResponse.toString());
                                return;
                            }
                            h.setHora_fin(horaFin + ":00");
                        }
                        if (activoStr != null && !activoStr.isEmpty()) {
                            h.setActivo(Boolean.parseBoolean(activoStr));
                        }

                        boolean resultado = hDao.update(h);

                        jsonResponse.addProperty("success", resultado);
                        jsonResponse.addProperty("message", resultado ? "Horario de Jumping actualizado correctamente" : "Error al actualizar horario de Jumping");

                        if (resultado) {
                            jsonResponse.add("data", gson.toJsonTree(h));
                            AuditoriaHelper.registrar(request, "UPDATE", "horario", id,
                                    "Actualización de horario id " + id);
                        }
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Horario de Jumping no encontrado");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y estado (activo) son requeridos");
                }
                out.print(jsonResponse.toString());

            } else if ("cambiarEstado".equals(action)) {
                String idParam = request.getParameter("id");
                String activoStr = request.getParameter("activo");

                if (idParam == null || idParam.isEmpty() || activoStr == null || activoStr.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y estado (activo) son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);
                Horario h = hDao.SearchById(id);

                if (h == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Horario de Jumping no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }

                h.setActivo(Boolean.parseBoolean(activoStr));
                boolean resultado = hDao.update(h);

                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Estado del horario de Jumping actualizado correctamente" : "Error al actualizar estado");
                if (resultado) {
                    jsonResponse.addProperty("activo", h.isActivo());
                    jsonResponse.add("data", gson.toJsonTree(h));
                    AuditoriaHelper.registrar(request, "UPDATE", "horario", id,
                            "Cambio de estado de horario id " + id + " a activo=" + h.isActivo());
                }
                out.print(jsonResponse.toString());

            } else if ("eliminar".equals(action)) {

                String idParam = request.getParameter("id_horario");

                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    boolean resultado = hDao.delete(id);

                    jsonResponse.addProperty("success", resultado);
                    jsonResponse.addProperty("message", resultado ? "Horario de Jumping eliminado correctamente" : "Error al eliminar horario de Jumping");
                    if (resultado) {
                        AuditoriaHelper.registrar(request, "DELETE", "horario", id, "Eliminación de horario id " + id);
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del horario requerido");
                }
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

    private boolean validarHora(String hora) {
        return hora.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }
}
