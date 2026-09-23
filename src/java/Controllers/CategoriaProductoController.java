package Controllers;

import Dao.CategoriaProductoDaoImpl;
import Interface.ICategoriaProducto;
import Model.CategoriaProducto;
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

@WebServlet(name = "CategoriaProductoController", urlPatterns = {"/CategoriaProductoController"})
public class CategoriaProductoController extends HttpServlet {

    private final ICategoriaProducto cDao = new CategoriaProductoDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        try (PrintWriter out = response.getWriter()) {

            if ("listar".equals(action)) {
                List<CategoriaProducto> lista = cDao.lista();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());
            } else if ("buscar".equals(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    CategoriaProducto c = cDao.SearchById(id);
                    if (c != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(c));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Categoría no encontrada");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID requerido");
                }
                out.print(jsonResponse.toString());
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Acción no válida");
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
        
        try (PrintWriter out = response.getWriter()){
            
            if ("insertar".equals(action)) {
                String nombre = request.getParameter("nombre");
                
                if (nombre != null && !nombre.trim().isEmpty()) {
                    CategoriaProducto c = new CategoriaProducto();
                    c.setNombre(nombre.trim());
                    
                    boolean resultado = cDao.insertar(c);
                    
                    jsonResponse.addProperty("success", resultado);
                    if (resultado) {
                        jsonResponse.addProperty("message", "Categoría insertada correctamente");
                        jsonResponse.addProperty("id", c.getId_categoria());
                        jsonResponse.add("data", gson.toJsonTree(c));
                        AuditoriaHelper.registrar(request, "INSERT", "categoria", c.getId_categoria(),
                                "Alta de categoría '" + c.getNombre() + "'");
                    } else {
                        jsonResponse.addProperty("message", "Error al insertar categoría");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "El nombre es requerido");
                }
                out.print(jsonResponse.toString());
            } else if ("actualizar".equals(action)){
                String idParam = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                
                if (idParam != null && !idParam.isEmpty() && nombre != null && !nombre.trim().isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    CategoriaProducto c = cDao.SearchById(id);
                    
                    if (c != null) {
                        c.setNombre(nombre.trim());
                        boolean resultado = cDao.update(c);
                        
                        jsonResponse.addProperty("success", resultado);
                        jsonResponse.addProperty("message", resultado ? "Categoria actualizada correctamente" : 
                                "Error al actualizar categoria");
                        if (resultado) {
                            jsonResponse.add("data", gson.toJsonTree(c));
                            AuditoriaHelper.registrar(request, "UPDATE", "categoria", id,
                                    "Actualización de categoría a '" + c.getNombre() + "'");
                        }
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Categoría no encontrada");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y nombre son requeridos");
                }
                out.print(jsonResponse.toString());
            } else if ("eliminar".equals(action)){
                String idParam = request.getParameter("id");
                
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    boolean resultado = cDao.delete(id);
                    
                    jsonResponse.addProperty("success", resultado);
                    jsonResponse.addProperty("message", resultado ? "Categoría eliminada correctamente" : "Error al eliminar categoría");
                    if (resultado) {
                        AuditoriaHelper.registrar(request, "DELETE", "categoria", id, "Eliminación de categoría id " + id);
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID requerido");
                }
                out.print(jsonResponse.toString());
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Acción no válida");
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
    public String getServletInfo() {
        return "Short description";
    }

}
