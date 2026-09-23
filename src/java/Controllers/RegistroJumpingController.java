package Controllers;

import Dao.ClienteDaoImpl;
import Dao.PersonaDaoImpl;
import Dao.HorarioDaoImpl;
import Dao.PlanJumpingDaoImpl;
import Dao.RegistroJumpingDaoImpl;
import Interface.ICliente;
import Interface.IPersona;
import Interface.IHorario;
import Interface.IPlanJumping;
import Interface.IRegistroJumping;
import Model.Cliente;
import Model.Persona;
import Model.Horario;
import Model.MetodoPago;
import Model.PlanJumping;
import Model.RegistroJumping;
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
import java.sql.Date;
import java.time.LocalDate;

@WebServlet(name = "RegistroJumpingController", urlPatterns = {"/RegistroJumpingController"})
public class RegistroJumpingController extends HttpServlet {

    private final IRegistroJumping rjDao = new RegistroJumpingDaoImpl();
    private final ICliente cDao = new ClienteDaoImpl();
    private final IPlanJumping pDao = new PlanJumpingDaoImpl();
    private final IHorario hDao = new HorarioDaoImpl();
    private final IPersona personaDao = new PersonaDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== REGISTRO JUMPING CONTROLLER (GET) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {
            if ("listar".equals(action)) {
                List<RegistroJumping> lista = rjDao.lista();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Registros Jumping listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());

            } else if ("buscar".equals(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    RegistroJumping r = rjDao.SearchById(id);
                    if (r != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(r));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Registro no encontrado");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID requerido");
                }
                out.print(jsonResponse.toString());

            } else if ("listarPorCliente".equals(action)) {
                String idClienteParam = request.getParameter("id_cliente");
                if (idClienteParam != null && !idClienteParam.isEmpty()) {
                    int idCliente = Integer.parseInt(idClienteParam);
                    List<RegistroJumping> lista = rjDao.SearchByClienteId(idCliente);
                    JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Registros del cliente listados correctamente");
                    jsonResponse.add("data", jsonArray);
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID de cliente requerido");
                }
                out.print(jsonResponse.toString());

            } else if ("listarPorFecha".equals(action)) {
                String fechaStr = request.getParameter("fecha");
                if (fechaStr != null && !fechaStr.isEmpty()) {
                    Date fecha = Date.valueOf(fechaStr);
                    List<RegistroJumping> lista = rjDao.getRegistrosByFecha(fecha);
                    JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Registros por fecha listados correctamente");
                    jsonResponse.add("data", jsonArray);
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Fecha requerida (formato: YYYY-MM-DD)");
                }
                out.print(jsonResponse.toString());

            } else if ("horariosDisponibles".equals(action)) {
                String fechaStr = request.getParameter("fecha");
                if (fechaStr != null && !fechaStr.isEmpty()) {
                    Date fecha = Date.valueOf(fechaStr);
                    List<Horario> horarios = rjDao.getHorariosDisponibles(fecha);
                    JsonArray jsonArray = gson.toJsonTree(horarios).getAsJsonArray();

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Horarios disponibles listados correctamente");
                    jsonResponse.add("data", jsonArray);
                } else {
                    Date fecha = Date.valueOf(LocalDate.now());
                    List<Horario> horarios = rjDao.getHorariosDisponibles(fecha);
                    JsonArray jsonArray = gson.toJsonTree(horarios).getAsJsonArray();

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Horarios disponibles para hoy listados correctamente");
                    jsonResponse.add("data", jsonArray);
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

        System.out.println("=== REGISTRO JUMPING CONTROLLER (POST) ===");
        System.out.println("Action: " + action);
        try (PrintWriter out = response.getWriter()) {
            
            if ("insertar".equals(action)) {
                String idClienteStr = request.getParameter("id_cliente");
                String idPlanStr = request.getParameter("id_plan");
                String idHorarioStr = request.getParameter("id_horario");
                String metodoPago = request.getParameter("metodo_pago");
                String fechaStr = request.getParameter("fecha");
                String montoStr = request.getParameter("monto");

                // Datos para registrar un cliente nuevo en el momento
                // (quien entra a la web NO necesita tener cuenta ni iniciar sesion)
                String nombre = request.getParameter("nombre");
                String apellido = request.getParameter("apellido");
                String documento = request.getParameter("documento");
                String numeroDoc = request.getParameter("numeroDoc");
                String telefono = request.getParameter("telefono");

                if (idPlanStr == null || idPlanStr.isEmpty()
                        || idHorarioStr == null || idHorarioStr.isEmpty()
                        || metodoPago == null || metodoPago.isEmpty()) {

                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "idPlan, idHorario y metodoPago son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }
                try {
                    int idPlan = Integer.parseInt(idPlanStr);
                    int idHorario = Integer.parseInt(idHorarioStr);

                    // ==========================================
                    // RESOLVER CLIENTE
                    // 1) por id_cliente  2) por documento (lo reutiliza)  3) lo crea
                    // ==========================================
                    Cliente cliente = null;

                    if (idClienteStr != null && !idClienteStr.isEmpty()) {
                        cliente = cDao.SearchById(Integer.parseInt(idClienteStr));
                        if (cliente == null) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message", "Cliente no encontrado");
                            out.print(jsonResponse.toString());
                            return;
                        }
                    } else {
                        if (nombre == null || nombre.isEmpty()
                                || apellido == null || apellido.isEmpty()
                                || documento == null || documento.isEmpty()
                                || numeroDoc == null || numeroDoc.isEmpty()) {

                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message",
                                    "Debe enviar id_cliente o los datos del nuevo cliente (nombre, apellido, documento, numeroDoc).");
                            out.print(jsonResponse.toString());
                            return;
                        }

                        // Si esa persona ya vino antes, reutilizamos su ficha
                        // en vez de duplicarla.
                        Persona existente = personaDao.SearchByDocumento(documento, numeroDoc);

                        if (existente != null) {
                            cliente = cDao.SearchByPersonaId(existente.getId_persona());
                            if (cliente == null) {
                                cliente = cDao.crearParaPersonaExistente(existente.getId_persona());
                            }
                        } else {
                            Persona persona = new Persona();
                            persona.setNombre(nombre);
                            persona.setApellido(apellido);
                            persona.setDocumento(documento);
                            persona.setNumeroDoc(numeroDoc);
                            persona.setTelefono(telefono);

                            Cliente nuevoCliente = new Cliente();
                            nuevoCliente.setPersona(persona);

                            if (cDao.insertar(nuevoCliente)) {
                                cliente = nuevoCliente;
                            }
                        }

                        if (cliente == null) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message", "No se pudo registrar al cliente");
                            out.print(jsonResponse.toString());
                            return;
                        }
                    }

                    PlanJumping plan = pDao.SearchById(idPlan);
                    if (plan == null) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Plan de Jumping no encontrado");
                        out.print(jsonResponse.toString());
                        return;
                    }
                    Horario horario = hDao.SearchById(idHorario);
                    if (horario == null) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Horario no encontrado");
                        out.print(jsonResponse.toString());
                        return;
                    }
                    if (!horario.isActivo()) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "El horario no está disponible");
                        out.print(jsonResponse.toString());
                        return;
                    }
                    Date fecha = fechaStr != null && !fechaStr.isEmpty()
                            ? Date.valueOf(fechaStr)
                            : Date.valueOf(LocalDate.now());

                    if (rjDao.isHorarioOcupado(idHorario, fecha)) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "El horario ya está ocupado para esta fecha");
                        out.print(jsonResponse.toString());
                        return;
                    }
                    RegistroJumping registro = new RegistroJumping();
                    registro.setCliente(cliente);
                    registro.setPlan(plan);
                    registro.setHorario(horario);
                    registro.setMetodoPago(MetodoPago.valueOf(metodoPago));
                    registro.setFechaIngreso(fecha);
                    registro.setMonto(montoStr != null && !montoStr.isEmpty()
                            ? Double.parseDouble(montoStr)
                            : plan.getPrecio());

                    boolean resultado = rjDao.insertar(registro);

                    jsonResponse.addProperty("success", resultado);
                    if (resultado) {
                        jsonResponse.addProperty("message", "Registro Jumping insertado correctamente");
                        jsonResponse.addProperty("id", registro.getId_registro());
                        jsonResponse.add("data", gson.toJsonTree(registro));
                        AuditoriaHelper.registrar(request, "INSERT", "registrojumping", registro.getId_registro(),
                                "Registro de sesión Jumping para el plan '" + plan.getNombre() + "' el " + fecha);
                    } else {
                        jsonResponse.addProperty("message", "Error al insertar registro");
                    }
                } catch (NumberFormatException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Error en formato de números");
                }
                out.print(jsonResponse.toString());

            } else if ("actualizar".equals(action)) {
                String idParam = request.getParameter("id");
                String idClienteStr = request.getParameter("id_cliente");
                String idPlanStr = request.getParameter("id_plan");
                String idHorarioStr = request.getParameter("id_horario");
                String metodoPago = request.getParameter("metodo_pago");
                String fechaStr = request.getParameter("fecha");
                String montoStr = request.getParameter("monto");

                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del registro requerido");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);
                RegistroJumping registro = rjDao.SearchById(id);

                if (registro == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Registro no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }
                try {
                    if (idClienteStr != null && !idClienteStr.isEmpty()) {
                        Cliente cliente = cDao.SearchById(Integer.parseInt(idClienteStr));
                        if (cliente != null) {
                            registro.setCliente(cliente);
                        }
                    }
                    if (idPlanStr != null && !idPlanStr.isEmpty()) {
                        PlanJumping plan = pDao.SearchById(Integer.parseInt(idPlanStr));
                        if (plan != null) {
                            registro.setPlan(plan);
                        }
                    }
                    if (idHorarioStr != null && !idHorarioStr.isEmpty()) {
                        Horario horario = hDao.SearchById(Integer.parseInt(idHorarioStr));
                        if (horario != null) {
                            registro.setHorario(horario);
                        }
                    }
                    if (metodoPago != null && !metodoPago.isEmpty()) {
                        registro.setMetodoPago(MetodoPago.valueOf(metodoPago));
                    }
                    if (fechaStr != null && !fechaStr.isEmpty()) {
                        registro.setFechaIngreso(Date.valueOf(fechaStr));
                    }
                    if (montoStr != null && !montoStr.isEmpty()) {
                        registro.setMonto(Double.parseDouble(montoStr));
                    }

                    boolean resultado = rjDao.update(registro);

                    jsonResponse.addProperty("success", resultado);
                    jsonResponse.addProperty("message", resultado ? "Registro actualizado correctamente" : "Error al actualizar registro");
                    if (resultado) {
                        jsonResponse.add("data", gson.toJsonTree(registro));
                        AuditoriaHelper.registrar(request, "UPDATE", "registrojumping", id,
                                "Actualización de registro Jumping id " + id);
                    }
                } catch (NumberFormatException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Error en formato de números");
                }
                out.print(jsonResponse.toString());

            } else if ("eliminar".equals(action)) {
                String idParam = request.getParameter("id");

                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del registro requerido");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);
                boolean resultado = rjDao.delete(id);

                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Registro eliminado correctamente" : "Error al eliminar registro");
                if (resultado) {
                    AuditoriaHelper.registrar(request, "DELETE", "registrojumping", id, "Eliminación de registro id " + id);
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
}
