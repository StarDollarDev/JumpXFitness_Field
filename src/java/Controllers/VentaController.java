package Controllers;

import Dao.ClienteDaoImpl;
import Dao.DetalleVentaDaoImpl;
import Dao.ProductoDaoImpl;
import Dao.VentaDaoImpl;
import Interface.ICliente;
import Interface.IDetalleVenta;
import Interface.IProducto;
import Interface.IVenta;
import Model.Cliente;
import Model.DetalleVenta;
import Model.MetodoPago;
import Model.Persona;
import Model.Producto;
import Model.Venta;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet(name = "VentaController", urlPatterns = {"/VentaController"})
public class VentaController extends HttpServlet {

    private final IVenta vDao = new VentaDaoImpl();
    private final ICliente cDao = new ClienteDaoImpl();
    private final IProducto pDao = new ProductoDaoImpl();
    private final IDetalleVenta dvDao = new DetalleVentaDaoImpl();
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

        System.out.println("=== VENTA CONTROLLER (GET) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {

            if ("listar".equals(action)) {
                List<Venta> lista = vDao.lista();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Ventas listadas correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());
            } else if ("buscar".equals(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    Venta v = vDao.SearchById(id);
                    if (v != null) {
                        // Obtener detalles de la venta
                        List<DetalleVenta> detalles = dvDao.SearchByVentaId(id);
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(v));
                        jsonResponse.add("detalles", gson.toJsonTree(detalles));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Venta no encontrada");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID requerido");
                }
                out.print(jsonResponse.toString());

            } else if ("listarPorCliente".equals(action)) {

                String idClienteParam = request.getParameter("idCliente");
                if (idClienteParam != null && !idClienteParam.isEmpty()) {
                    int idCliente = Integer.parseInt(idClienteParam);
                    List<Venta> lista = vDao.SearchByClienteId(idCliente);
                    JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Ventas del cliente listadas correctamente");
                    jsonResponse.add("data", jsonArray);
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID de cliente requerido");
                }
                out.print(jsonResponse.toString());

            } else if ("listarPorFecha".equals(action)) {
                String fechaStr = request.getParameter("fecha");
                if (fechaStr != null && !fechaStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date fecha = sdf.parse(fechaStr);
                        List<Venta> lista = vDao.SearchByFecha(fecha);
                        JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                        jsonResponse.addProperty("success", true);
                        jsonResponse.addProperty("message", "Ventas por fecha listadas correctamente");
                        jsonResponse.add("data", jsonArray);
                    } catch (Exception e) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Formato de fecha inválido. Use YYYY-MM-DD");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Fecha requerida (formato: YYYY-MM-DD)");
                }
                out.print(jsonResponse.toString());

            } else if ("listarPorRango".equals(action)) {
                String fechaInicioStr = request.getParameter("fechaInicio");
                String fechaFinStr = request.getParameter("fechaFin");

                if (fechaInicioStr != null && !fechaInicioStr.isEmpty()
                        && fechaFinStr != null && !fechaFinStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date fechaInicio = sdf.parse(fechaInicioStr);
                        Date fechaFin = sdf.parse(fechaFinStr);

                        List<Venta> lista = vDao.SearchByDateRange(fechaInicio, fechaFin);
                        JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                        jsonResponse.addProperty("success", true);
                        jsonResponse.addProperty("message", "Ventas por rango listadas correctamente");
                        jsonResponse.add("data", jsonArray);
                    } catch (Exception e) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Formato de fecha inválido. Use YYYY-MM-DD");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "fechaInicio y fechaFin son requeridos");
                }
                out.print(jsonResponse.toString());

            } else if ("resumen".equals(action)) {
                JsonObject resumen = new JsonObject();

                // Total de ventas
                List<Venta> todas = vDao.lista();
                double totalGeneral = todas.stream().mapToDouble(Venta::getTotal).sum();
                resumen.addProperty("totalVentas", todas.size());
                resumen.addProperty("totalIngresos", totalGeneral);

                // Ventas de hoy
                List<Venta> ventasHoy = vDao.SearchByFecha(new Date());
                double totalHoy = ventasHoy.stream().mapToDouble(Venta::getTotal).sum();
                resumen.addProperty("ventasHoy", ventasHoy.size());
                resumen.addProperty("ingresosHoy", totalHoy);

                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", resumen);
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

        System.out.println("=== VENTA CONTROLLER (POST) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {
            if ("insertar".equals(action)) {

                String idClienteStr = request.getParameter("idCliente");
                String metodoPago = request.getParameter("metodoPago");
                String productosJson = request.getParameter("productos");

                // Datos para cliente nuevo (si no existe)
                String nombre = request.getParameter("nombre");
                String apellido = request.getParameter("apellido");
                String documento = request.getParameter("documento");
                String numeroDoc = request.getParameter("numeroDoc");
                String telefono = request.getParameter("telefono");

                // Validar campos requeridos
                if (metodoPago == null || metodoPago.isEmpty()
                        || productosJson == null || productosJson.isEmpty()) {

                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "metodoPago y productos son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }
                try {
                    Cliente cliente = null;
                    boolean clienteNuevo = false;

                    // CASO 1: Se envía idCliente (cliente existente)
                    if (idClienteStr != null && !idClienteStr.isEmpty()) {
                        int idCliente = Integer.parseInt(idClienteStr);
                        cliente = cDao.SearchById(idCliente);
                        if (cliente == null) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message", "Cliente no encontrado");
                            out.print(jsonResponse.toString());
                            return;
                        }
                    } // CASO 2: No se envía idCliente, crear cliente nuevo
                    else if (nombre != null && !nombre.trim().isEmpty()
                            && apellido != null && !apellido.trim().isEmpty()
                            && documento != null && !documento.trim().isEmpty()
                            && numeroDoc != null && !numeroDoc.trim().isEmpty()) {

                        // Crear persona
                        Persona persona = new Persona();
                        persona.setNombre(nombre.trim());
                        persona.setApellido(apellido.trim());
                        persona.setDocumento(documento.trim());
                        persona.setNumeroDoc(numeroDoc.trim());
                        persona.setTelefono(telefono != null ? telefono.trim() : "");

                        // Crear cliente
                        Cliente nuevoCliente = new Cliente();
                        nuevoCliente.setPersona(persona);

                        boolean clienteCreado = cDao.insertar(nuevoCliente);
                        if (!clienteCreado) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message", "Error al crear el cliente");
                            out.print(jsonResponse.toString());
                            return;
                        }
                        cliente = nuevoCliente;
                        clienteNuevo = true;
                    }
                    // Si no hay cliente y no hay datos, es venta anónima

                    // Parsear productos del JSON
                    JsonArray productosArray = gson.fromJson(productosJson, JsonArray.class);

                    if (productosArray.size() == 0) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Debe incluir al menos un producto");
                        out.print(jsonResponse.toString());
                        return;
                    }
                    // Crear la venta
                    Venta venta = new Venta();
                    venta.setCliente(cliente);
                    venta.setMetodoPago(MetodoPago.valueOf(metodoPago));
                    venta.setFecha(new Date());

                    // Calcular total y crear detalles
                    double total = 0;
                    for (int i = 0; i < productosArray.size(); i++) {
                        JsonObject obj = productosArray.get(i).getAsJsonObject();

                        if (!obj.has("idProducto") || !obj.has("cantidad") || !obj.has("precio")
                                || obj.get("idProducto").isJsonNull()
                                || obj.get("cantidad").isJsonNull()
                                || obj.get("precio").isJsonNull()) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message",
                                    "Cada producto debe incluir idProducto, cantidad y precio (producto #" + (i + 1) + " incompleto)");
                            out.print(jsonResponse.toString());
                            return;
                        }

                        int idProducto = obj.get("idProducto").getAsInt();
                        int cantidad = obj.get("cantidad").getAsInt();
                        double precio = obj.get("precio").getAsDouble();

                        // Verificar producto
                        Producto producto = pDao.SearchById(idProducto);
                        if (producto == null) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message", "Producto con ID " + idProducto + " no encontrado");
                            out.print(jsonResponse.toString());
                            return;
                        }

                        // Verificar stock
                        if (producto.getStock() < cantidad) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message", "Stock insuficiente para: " + producto.getNombre() + ". Stock: " + producto.getStock());
                            out.print(jsonResponse.toString());
                            return;
                        }

                        double subtotal = precio * cantidad;
                        total += subtotal;
                    }

                    venta.setTotal(total);

                    // Insertar venta
                    boolean resultado = vDao.insertar(venta);

                    if (!resultado) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Error al insertar venta");
                        out.print(jsonResponse.toString());
                        return;
                    }

                    // Insertar detalles y actualizar stock
                    for (int i = 0; i < productosArray.size(); i++) {
                        JsonObject obj = productosArray.get(i).getAsJsonObject();

                        int idProducto = obj.get("idProducto").getAsInt();
                        int cantidad = obj.get("cantidad").getAsInt();
                        double precio = obj.get("precio").getAsDouble();

                        Producto producto = pDao.SearchById(idProducto);
                        double subtotal = precio * cantidad;

                        DetalleVenta detalle = new DetalleVenta();
                        detalle.setVenta(venta);
                        detalle.setProducto(producto);
                        detalle.setCantidad(cantidad);
                        detalle.setPrecio(precio);
                        detalle.setSubtotal(subtotal);

                        dvDao.insertar(detalle);

                        // Actualizar stock
                        int nuevoStock = producto.getStock() - cantidad;
                        pDao.updateStock(idProducto, nuevoStock);
                    }

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", clienteNuevo ? "Cliente creado y venta insertada correctamente" : "Venta insertada correctamente");
                    jsonResponse.addProperty("idVenta", venta.getId_venta());
                    jsonResponse.addProperty("total", total);
                    if (cliente != null) {
                        jsonResponse.addProperty("idCliente", cliente.getId_cliente());
                    }
                    jsonResponse.add("data", gson.toJsonTree(venta));
                    AuditoriaHelper.registrar(request, "INSERT", "venta", venta.getId_venta(),
                            "Venta por " + total + " (" + productosArray.size() + " producto(s), " + metodoPago + ")");
                    out.print(jsonResponse.toString());

                } catch (IllegalArgumentException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Método de pago inválido: " + e.getMessage());
                    out.print(jsonResponse.toString());
                } catch (Exception e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Error: " + e.getMessage());
                    out.print(jsonResponse.toString());
                }

            } else if ("eliminar".equals(action)) {
                String idParam = request.getParameter("id");

                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID de la venta requerido");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);

                // Verificar que exista
                Venta v = vDao.SearchById(id);
                if (v == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Venta no encontrada");
                    out.print(jsonResponse.toString());
                    return;
                }

                // Primero eliminar detalles
                List<DetalleVenta> detalles = dvDao.SearchByVentaId(id);
                for (DetalleVenta dv : detalles) {
                    dvDao.delete(dv.getId_detalle());
                }

                // Luego eliminar venta
                boolean resultado = vDao.delete(id);

                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Venta eliminada correctamente" : "Error al eliminar venta");
                if (resultado) {
                    AuditoriaHelper.registrar(request, "DELETE", "venta", id,
                            "Eliminación de venta id " + id + " por un total de " + v.getTotal());
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
