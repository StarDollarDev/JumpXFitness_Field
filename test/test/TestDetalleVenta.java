
package test;

import Dao.DetalleVentaDaoImpl;
import Model.DetalleVenta;
import Model.Producto;
import Model.Venta;

public class TestDetalleVenta {
    
    static DetalleVentaDaoImpl dao = new DetalleVentaDaoImpl();

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println(" TEST CRUD DETALLE VENTA");
        System.out.println("====================================");
        
        insertarDetalleVenta();
        //listarDetallesVenta();
        //buscarDetalleVenta(1);
        //actualizarDetalleVenta(1);
        //eliminarDetalleVenta(1);
    }
    
    // ================= INSERT =================
    public static void insertarDetalleVenta() {
        System.out.println("\n=== INSERTAR DETALLE VENTA ===");
        DetalleVenta dv = new DetalleVenta();
        
        Venta v = new Venta();
        v.setId_venta(1);
        
        Producto p = new Producto();
        p.setId_producto(2);
        
        dv.setVenta(v);
        dv.setProducto(p);
        dv.setCantidad(2);
        dv.setPrecio(15.50);
        dv.setSubtotal(31.00);

        boolean resultado = dao.insertar(dv);
        if (resultado) {
            System.out.println(" Detalle de venta insertado correctamente con ID: " + dv.getId_detalle());
        } else {
            System.out.println(" Error al insertar detalle de venta");
        }
    }
    
    // ================= LISTAR =================
    public static void listarDetallesVenta() {
        System.out.println("\n=== LISTA DE DETALLES DE VENTA ===");
        var lista = dao.lista();
        if (lista.isEmpty()) {
            System.out.println("No existen registros");
        } else {
            System.out.printf("%-5s %-10s %-10s %-10s %-10s %-10s%n",
                    "ID", "ID_VENTA", "ID_PROD", "CANT", "PRECIO", "SUBTOTAL");
            System.out.println("----------------------------------------------------------");
            for (DetalleVenta dv : lista) {
                System.out.printf("%-5d %-10d %-10d %-10d %-10.2f %-10.2f%n",
                        dv.getId_detalle(),
                        dv.getVenta().getId_venta(),
                        dv.getProducto().getId_producto(),
                        dv.getCantidad(),
                        dv.getPrecio(),
                        dv.getSubtotal());
            }
        }
    }
    
    // ================= BUSCAR =================
    public static void buscarDetalleVenta(int id) {
        System.out.println("\n=== BUSCAR DETALLE VENTA ===");
        DetalleVenta dv = dao.SearchById(id);
        if (dv != null) {
            System.out.println("ID Detalle: " + dv.getId_detalle());
            System.out.println("ID Venta: " + dv.getVenta().getId_venta());
            System.out.println("ID Producto: " + dv.getProducto().getId_producto());
            System.out.println("Cantidad: " + dv.getCantidad());
            System.out.println("Precio: " + dv.getPrecio());
            System.out.println("Subtotal: " + dv.getSubtotal());
        } else {
            System.out.println(" Detalle de venta no encontrado");
        }
    }
    
    // ================= ACTUALIZAR =================
    public static void actualizarDetalleVenta(int id) {
        System.out.println("\n=== ACTUALIZAR DETALLE VENTA ===");
        DetalleVenta dv = dao.SearchById(id);
        if (dv != null) {
            dv.setCantidad(5);
            dv.setSubtotal(77.50);
            
            boolean resultado = dao.update(dv);
            if (resultado) {
                System.out.println(" Detalle de venta actualizado correctamente");
            } else {
                System.out.println(" Error al actualizar detalle de venta");
            }
        } else {
            System.out.println(" Detalle de venta no encontrado");
        }
    }
    
    // ================= ELIMINAR =================
    public static void eliminarDetalleVenta(int id) {
        System.out.println("\n=== ELIMINAR DETALLE VENTA ===");
        boolean resultado = dao.delete(id);
        if (resultado) {
            System.out.println(" Detalle de venta eliminado correctamente");
        } else {
            System.out.println(" Error al eliminar detalle de venta");
        }
    }
}
