
package test;

import Dao.CategoriaProductoDaoImpl;
import Model.CategoriaProducto;

public class TestCategoriaProducto {
    
    static CategoriaProductoDaoImpl dao = new CategoriaProductoDaoImpl();

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println(" TEST CRUD CATEGORIA PRODUCTO");
        System.out.println("====================================");
        
        //insertarCategoria();
        //listarCategorias();
        //buscarCategoria(2);
        //actualizarCategoria(2);
        //eliminarCategoria(2);
    }
    
    // ================= INSERT =================
    public static void insertarCategoria() {
        System.out.println("\n=== INSERTAR CATEGORIA ===");
        CategoriaProducto c = new CategoriaProducto();
        c.setNombre("Bebidas");

        boolean resultado = dao.insertar(c);
        if (resultado) {
            System.out.println(" Categoria insertada correctamente con ID: " + c.getId_categoria());
        } else {
            System.out.println(" Error al insertar categoria");
        }
    }
    
    // ================= LISTAR =================
    public static void listarCategorias() {
        System.out.println("\n=== LISTA DE CATEGORIAS ===");
        var lista = dao.lista();
        if (lista.isEmpty()) {
            System.out.println("No existen registros");
        } else {
            System.out.printf("%-5s %-30s%n", "ID", "NOMBRE");
            System.out.println("----------------------------------------");
            for (CategoriaProducto c : lista) {
                System.out.printf("%-5d %-30s%n", c.getId_categoria(), c.getNombre());
            }
        }
    }
    
    // ================= BUSCAR =================
    public static void buscarCategoria(int id) {
        System.out.println("\n=== BUSCAR CATEGORIA ===");
        CategoriaProducto c = dao.SearchById(id);
        if (c != null) {
            System.out.println("ID: " + c.getId_categoria());
            System.out.println("Nombre: " + c.getNombre());
        } else {
            System.out.println(" Categoria no encontrada");
        }
    }
    
    // ================= ACTUALIZAR =================
    public static void actualizarCategoria(int id) {
        System.out.println("\n=== ACTUALIZAR CATEGORIA ===");
        CategoriaProducto c = dao.SearchById(id);
        if (c != null) {
            c.setNombre("Bebidas Naturales");
            boolean resultado = dao.update(c);
            if (resultado) {
                System.out.println(" Categoria actualizada correctamente");
            } else {
                System.out.println(" Error al actualizar categoria");
            }
        } else {
            System.out.println(" Categoria no encontrada");
        }
    }
    
    // ================= ELIMINAR =================
    public static void eliminarCategoria(int id) {
        System.out.println("\n=== ELIMINAR CATEGORIA ===");
        boolean resultado = dao.delete(id);
        if (resultado) {
            System.out.println(" Categoria eliminada correctamente");
        } else {
            System.out.println(" Error al eliminar categoria");
        }
    }
}
