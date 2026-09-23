
package Controllers;

import Dao.PlanJumpingDaoImpl;
import Interface.IPlanJumping;
import Model.PlanJumping;
import Util.AuditoriaHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.util.List;

@MultipartConfig
@WebServlet(name = "PlanJumpingController", urlPatterns = {"/PlanJumpingController"})
public class PlanJumpingController extends HttpServlet {

    private final IPlanJumping pDao = new PlanJumpingDaoImpl();
    private final Gson gson = new Gson();
    private static final String CARPETA_IMAGENES = "assets/img/planes";

    /**
     * Guarda la imagen recibida en el Part "imagen" dentro de assets/img/planes,
     * tanto en la carpeta fuente (web/) como en la desplegada (build/web/), y
     * devuelve la ruta relativa a guardar en la BD. Si no llegó imagen, devuelve null.
     */
    private String guardarImagen(HttpServletRequest request) throws IOException, ServletException {
        Part part = request.getPart("imagen");
        if (part == null || part.getSize() <= 0) {
            return null;
        }
        String nombreArchivo = System.currentTimeMillis() + "_" + part.getSubmittedFileName();

        String pathBuild = getServletContext().getRealPath("/") + CARPETA_IMAGENES + File.separator;
        String pathSource = pathBuild.replace("build" + File.separator + "web", "web");

        new File(pathBuild).mkdirs();
        new File(pathSource).mkdirs();

        try (InputStream input = part.getInputStream()) {
            java.nio.file.Files.copy(input, new File(pathBuild + nombreArchivo).toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        if (!pathSource.equals(pathBuild)) {
            part.write(pathSource + nombreArchivo);
        }
        return CARPETA_IMAGENES + "/" + nombreArchivo;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== PLAN JUMPING CONTROLLER (GET) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {
            if ("listar".equals(action)) {
                List<PlanJumping> lista = pDao.lista();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Planes de Jumping listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());

            } else if ("listarActivos".equals(action)) {
                List<PlanJumping> lista = pDao.listaActivos();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Planes de Jumping activos listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());

            } else if ("buscar".equals(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    PlanJumping p = pDao.SearchById(id);
                    if (p != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(p));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Plan de Jumping no encontrado");
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

        System.out.println("=== PLAN JUMPING CONTROLLER (POST) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {
            if ("insertar".equals(action)) {
                String nombre = request.getParameter("nombre");
                String precioStr = request.getParameter("precio");
                String cantidadPersonasStr = request.getParameter("cantidad_personas");
                String diasVigenciaStr = request.getParameter("dias_vigencia");
                String activoStr = request.getParameter("activo");

                if (nombre == null || nombre.trim().isEmpty()
                        || precioStr == null || precioStr.isEmpty()
                        || cantidadPersonasStr == null || cantidadPersonasStr.isEmpty()
                        || diasVigenciaStr == null || diasVigenciaStr.isEmpty()) {

                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Todos los campos son requeridos: nombre, precio, cantidadPersonas, diasVigencia");
                    out.print(jsonResponse.toString());
                    return;
                }
                try {
                    PlanJumping p = new PlanJumping();
                    p.setNombre(nombre.trim());
                    p.setPrecio(Double.parseDouble(precioStr));
                    p.setCantidadPersonas(Integer.parseInt(cantidadPersonasStr));
                    p.setDiasVigencia(Integer.parseInt(diasVigenciaStr));
                    p.setActivo(activoStr != null ? Boolean.parseBoolean(activoStr) : true);

                    String rutaImagen = guardarImagen(request);
                    if (rutaImagen != null) {
                        p.setImagen(rutaImagen);
                    }

                    boolean resultado = pDao.insertar(p);

                    jsonResponse.addProperty("success", resultado);
                    if (resultado) {
                        jsonResponse.addProperty("message", "Plan de Jumping insertado correctamente");
                        jsonResponse.addProperty("id", p.getId_plan());
                        jsonResponse.add("data", gson.toJsonTree(p));
                        AuditoriaHelper.registrar(request, "INSERT", "planjumping", p.getId_plan(),
                                "Alta de plan '" + p.getNombre() + "' (S/ " + p.getPrecio() + ")");
                    } else {
                        jsonResponse.addProperty("message", "Error al insertar plan de Jumping");
                    }
                } catch (NumberFormatException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Error en formato de números: precio, cantidadPersonas y diasVigencia deben ser numéricos");
                }
                out.print(jsonResponse.toString());

            } else if ("actualizar".equals(action)) {
                String idParam = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                String precioStr = request.getParameter("precio");
                String cantidadPersonasStr = request.getParameter("cantidad_personas");
                String diasVigenciaStr = request.getParameter("dias_vigencia");
                String activoStr = request.getParameter("activo");

                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del plan requerido");
                    out.print(jsonResponse.toString());
                    return;
                }
                int id = Integer.parseInt(idParam);
                PlanJumping p = pDao.SearchById(id);

                if (p == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Plan de Jumping no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }

                try {
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        p.setNombre(nombre.trim());
                    }
                    if (precioStr != null && !precioStr.isEmpty()) {
                        p.setPrecio(Double.parseDouble(precioStr));
                    }
                    if (cantidadPersonasStr != null && !cantidadPersonasStr.isEmpty()) {
                        p.setCantidadPersonas(Integer.parseInt(cantidadPersonasStr));
                    }
                    if (diasVigenciaStr != null && !diasVigenciaStr.isEmpty()) {
                        p.setDiasVigencia(Integer.parseInt(diasVigenciaStr));
                    }
                    if (activoStr != null && !activoStr.isEmpty()) {
                        p.setActivo(Boolean.parseBoolean(activoStr));
                    }

                    String rutaImagen = guardarImagen(request);
                    if (rutaImagen != null) {
                        p.setImagen(rutaImagen);
                    }
                    // si no se subió una imagen nueva, p.getImagen() ya conserva la anterior
                    // (viene precargada de pDao.SearchById(id) unas líneas más arriba)

                    boolean resultado = pDao.update(p);

                    jsonResponse.addProperty("success", resultado);
                    jsonResponse.addProperty("message", resultado ? "Plan de Jumping actualizado correctamente" : "Error al actualizar plan de Jumping");
                    if (resultado) {
                        jsonResponse.add("data", gson.toJsonTree(p));
                        AuditoriaHelper.registrar(request, "UPDATE", "planjumping", id,
                                "Actualización de plan '" + p.getNombre() + "'");
                    }
                } catch (NumberFormatException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Error en formato de números: precio, cantidadPersonas y diasVigencia deben ser numéricos");
                }
                out.print(jsonResponse.toString());

            } else if ("cambiarEstado".equals(action)){
                String idParam = request.getParameter("id");
                String activoStr = request.getParameter("activo");
                
                if (idParam == null || idParam.isEmpty() || activoStr == null || activoStr.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y estado (activo) son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                PlanJumping p = pDao.SearchById(id);
                
                if (p == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Plan de Jumping no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                p.setActivo(Boolean.parseBoolean(activoStr));
                boolean resultado = pDao.update(p);
                
                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Estado del plan actualizado correctamente" : "Error al actualizar estado");
                if (resultado) {
                    jsonResponse.addProperty("activo", p.isActivo());
                    jsonResponse.add("data", gson.toJsonTree(p));
                    AuditoriaHelper.registrar(request, "UPDATE", "planjumping", id,
                            "Cambio de estado de plan '" + p.getNombre() + "' a activo=" + p.isActivo());
                }
                out.print(jsonResponse.toString());

            } else if ("eliminar".equals(action)){
                String idParam = request.getParameter("id");
                
                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del plan requerido");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                boolean resultado = pDao.delete(id);
                
                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Plan de Jumping eliminado correctamente" : "Error al eliminar plan de Jumping");
                if (resultado) {
                    AuditoriaHelper.registrar(request, "DELETE", "planjumping", id, "Eliminación de plan id " + id);
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
