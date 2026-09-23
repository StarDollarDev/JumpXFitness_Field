
package Controllers;

import Dao.CategoriaProductoDaoImpl;
import Dao.ProductoDaoImpl;
import Interface.ICategoriaProducto;
import Interface.IProducto;
import Model.CategoriaProducto;
import Model.Producto;
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

@WebServlet(name = "ProductoController", urlPatterns = {"/ProductoController"})
public class ProductoController extends HttpServlet {
    
    private final IProducto pDao = new ProductoDaoImpl();
    private final ICategoriaProducto cDao = new CategoriaProductoDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== PRODUCTO CONTROLLER (GET) ===");
        System.out.println("Action: " + action);
        
        try (PrintWriter out = response.getWriter()){
            if ("listar".equals(action)) {
                List<Producto> lista = pDao.lista();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();
                
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Productos listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());

            } else if ("listarConStock".equals(action)){
                List<Producto> lista = pDao.SearchWithStock();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();
                
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Productos con stock listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());
                
            } else if ("listarPorCategoria".equals(action)){
                String idCategoriaParam = request.getParameter("idCategoria");
                if (idCategoriaParam != null && !idCategoriaParam.isEmpty()) {
                    int idCategoria = Integer.parseInt(idCategoriaParam);
                    List<Producto> lista = pDao.SearchByCategoriaId(idCategoria);
                    JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();
                    
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Productos por categoría listados correctamente");
                    jsonResponse.add("data", jsonArray);
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID de categoría requerido");
                }
                out.print(jsonResponse.toString());
                
            } else if ("buscar".equals(action)){
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    Producto p = pDao.SearchById(id);
                    if (p != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(p));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Producto no encontrado");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID requerido");
                }
                out.print(jsonResponse.toString());
                
            } else if ("buscarPorNombre".equals(action)){
                String nombre = request.getParameter("nombre");
                if (nombre != null && !nombre.trim().isEmpty()) {
                    Producto p = pDao.SearchByNombreExact(nombre.trim());
                    if (p != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(p));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Producto no encontrado");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Nombre requerido");
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

        System.out.println("=== PRODUCTO CONTROLLER (POST) ===");
        System.out.println("Action: " + action);
        
        try (PrintWriter out = response.getWriter()){
            if ("insertar".equals(action)) {
                String nombre = request.getParameter("nombre");
                String descripcion = request.getParameter("descripcion");
                String precioCompraStr = request.getParameter("precio_compra");
                String precioVentaStr = request.getParameter("precio_venta");
                String stockStr = request.getParameter("stock");
                String idCategoriaStr = request.getParameter("id_categoria");
                
                // Validar campos requeridos
                if (nombre == null || nombre.trim().isEmpty() ||
                    precioVentaStr == null || precioVentaStr.isEmpty() ||
                    idCategoriaStr == null || idCategoriaStr.isEmpty()) {
                    
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Los campos nombre, precioVenta e idCategoria son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }
                try {
                    Producto p = new Producto();
                    p.setNombre(nombre.trim());
                    p.setDescripcion(descripcion != null ? descripcion.trim() : "");
                    p.setPrecioCompra(precioCompraStr != null && !precioCompraStr.isEmpty() ? Double.parseDouble(precioCompraStr) : 0.0);
                    p.setPrecioVenta(Double.parseDouble(precioVentaStr));
                    p.setStock(stockStr != null && !stockStr.isEmpty() ? Integer.parseInt(stockStr) : 0);
                    
                    CategoriaProducto categoria = new CategoriaProducto();
                    categoria.setId_categoria(Integer.parseInt(idCategoriaStr));
                    p.setCategoriaProducto(categoria);
                    
                    boolean resultado = pDao.insertar(p);
                    
                    jsonResponse.addProperty("success", resultado);
                    if (resultado) {
                        jsonResponse.addProperty("message", "Producto insertado correctamente");
                        jsonResponse.addProperty("id", p.getId_producto());
                        jsonResponse.add("data", gson.toJsonTree(p));
                        AuditoriaHelper.registrar(request, "INSERT", "producto", p.getId_producto(),
                                "Alta de producto '" + p.getNombre() + "' (stock inicial " + p.getStock() + ")");
                    } else {
                        jsonResponse.addProperty("message", "Error al insertar producto");
                    }
                } catch (Exception e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Error en formato de números");
                }
                out.print(jsonResponse.toString());
                
            } else if ("actualizar".equals(action)){
                String idParam = request.getParameter("id");
                String nombre = request.getParameter("nombre");
                String descripcion = request.getParameter("descripcion");
                String precioCompraStr = request.getParameter("precio_compra");
                String precioVentaStr = request.getParameter("precio_venta");
                String stockStr = request.getParameter("stock");
                String idCategoriaStr = request.getParameter("id_categoria");
                
                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del producto requerido");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                Producto p = pDao.SearchById(id);
                
                if (p == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Producto no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }
                try {
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        p.setNombre(nombre.trim());
                    }
                    if (descripcion != null) {
                        p.setDescripcion(descripcion.trim());
                    }
                    if (precioCompraStr != null && !precioCompraStr.isEmpty()) {
                        p.setPrecioCompra(Double.parseDouble(precioCompraStr));
                    }
                    if (precioVentaStr != null && !precioVentaStr.isEmpty()) {
                        p.setPrecioVenta(Double.parseDouble(precioVentaStr));
                    }
                    if (stockStr != null && !stockStr.isEmpty()) {
                        p.setStock(Integer.parseInt(stockStr));
                    }
                    if (idCategoriaStr != null && !idCategoriaStr.isEmpty()) {
                        CategoriaProducto categoria = new CategoriaProducto();
                        categoria.setId_categoria(Integer.parseInt(idCategoriaStr));
                        p.setCategoriaProducto(categoria);
                    }
                    
                    boolean resultado = pDao.update(p);
                    
                    jsonResponse.addProperty("success", resultado);
                    jsonResponse.addProperty("message", resultado ? "Producto actualizado correctamente" : "Error al actualizar producto");
                    if (resultado) {
                        jsonResponse.add("data", gson.toJsonTree(p));
                        AuditoriaHelper.registrar(request, "UPDATE", "producto", id,
                                "Actualización de producto '" + p.getNombre() + "'");
                    }
                } catch (Exception e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Error en formato de números");
                }
                out.print(jsonResponse.toString());
                
            } else if ("actualizarStock".equals(action)){
                String idParam = request.getParameter("id");
                String stockStr = request.getParameter("stock");
                
                if (idParam == null || idParam.isEmpty() || stockStr == null || stockStr.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y stock son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                int stock = Integer.parseInt(stockStr);
                
                if (stock < 0) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "El stock no puede ser negativo");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                boolean resultado = pDao.updateStock(id, stock);
                
                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Stock actualizado correctamente" : "Error al actualizar stock");
                if (resultado) {
                    jsonResponse.addProperty("nuevoStock", stock);
                    AuditoriaHelper.registrar(request, "UPDATE", "producto", id,
                            "Stock fijado manualmente a " + stock);
                }
                out.print(jsonResponse.toString());
                
            } else if ("aumentarStock".equals(action)){
                
                String idParam = request.getParameter("id");
                String cantidadStr = request.getParameter("cantidad");
                
                if (idParam == null || idParam.isEmpty() || cantidadStr == null || cantidadStr.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y cantidad son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                int cantidad = Integer.parseInt(cantidadStr);
                
                if (cantidad <= 0) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "La cantidad debe ser mayor a 0");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                Producto p = pDao.SearchById(id);
                if (p == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Producto no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int nuevoStock = p.getStock() + cantidad;
                boolean resultado = pDao.updateStock(id, nuevoStock);
                
                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Stock aumentado correctamente" : "Error al aumentar stock");
                if (resultado) {
                    jsonResponse.addProperty("stockAnterior", p.getStock());
                    jsonResponse.addProperty("nuevoStock", nuevoStock);
                    AuditoriaHelper.registrar(request, "UPDATE", "producto", id,
                            "Ingreso de stock: +" + cantidad + " (" + p.getStock() + " -> " + nuevoStock + ") de '" + p.getNombre() + "'");
                }
                out.print(jsonResponse.toString());

            } else if ("reducirStock".equals(action)){
                
                String idParam = request.getParameter("id");
                String cantidadStr = request.getParameter("cantidad");
                
                if (idParam == null || idParam.isEmpty() || cantidadStr == null || cantidadStr.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y cantidad son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                int cantidad = Integer.parseInt(cantidadStr);
                
                if (cantidad <= 0) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "La cantidad debe ser mayor a 0");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                Producto p = pDao.SearchById(id);
                if (p == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Producto no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                if (p.getStock() < cantidad) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Stock insuficiente. Stock actual: " + p.getStock());
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int nuevoStock = p.getStock() - cantidad;
                boolean resultado = pDao.updateStock(id, nuevoStock);
                
                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Stock reducido correctamente" : "Error al reducir stock");
                if (resultado) {
                    jsonResponse.addProperty("stockAnterior", p.getStock());
                    jsonResponse.addProperty("nuevoStock", nuevoStock);
                    AuditoriaHelper.registrar(request, "UPDATE", "producto", id,
                            "Salida de stock: -" + cantidad + " (" + p.getStock() + " -> " + nuevoStock + ") de '" + p.getNombre() + "'");
                }
                out.print(jsonResponse.toString());
                
            } else if ("eliminar".equals(action)){
                String idParam = request.getParameter("id");
                
                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del producto requerido");
                    out.print(jsonResponse.toString());
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                boolean resultado = pDao.delete(id);
                
                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Producto eliminado correctamente" : "Error al eliminar producto");
                if (resultado) {
                    AuditoriaHelper.registrar(request, "DELETE", "producto", id, "Eliminación de producto id " + id);
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
