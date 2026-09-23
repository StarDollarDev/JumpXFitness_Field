
package test;

import Dao.PersonaDaoImpl;
import Model.Persona;
import Model.Rol;
import Model.Usuario;

public class TestPersona {
    
    static PersonaDaoImpl dao = new PersonaDaoImpl();

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println(" TEST CRUD PERSONA");
        System.out.println("====================================");
        
         insertarPersona();
        // listarPersonas();
        // buscarPersona(1);
        // actualizarPersona(1);
        // eliminarPersona(2);

    }
    
    // ================= INSERT =================
    public static void insertarPersona() {
        System.out.println("\n=== INSERTAR PERSONA ===");
        
        
        Persona p = new Persona();
        p.setNombre("Jump");
        p.setApellido("Fitness");
        p.setDocumento("DNI");
        p.setNumeroDoc("99999999"); 
        p.setTelefono("NULL");

        Usuario u = new Usuario();
        u.setUsuario("JumpxFitness");
        u.setContraseña("jumping123");
        u.setRol(Rol.ADMIN);

        boolean resultado = dao.insertSoloPersona(p, u);
        if (resultado) {
            System.out.println(" Persona insertada correctamente con ID: " + p.getId_persona());
            System.out.println(" Documento: " + p.getDocumento() + " - " + p.getNumeroDoc());
            System.out.println(" Usuario asociado: " + u.getUsuario());
        } else {
            System.out.println(" Error al insertar persona");
        }
    }
    
    // ================= LISTAR =================
    public static void listarPersonas() {
        System.out.println("\n=== LISTA DE PERSONAS ===");
        var lista = dao.lista();
        if (lista.isEmpty()) {
            System.out.println("No existen registros");
        } else {
            System.out.printf("%-5s %-20s %-20s %-15s%n", "ID", "NOMBRE", "APELLIDO", "TELEFONO");
            System.out.println("--------------------------------------------------------------");
            for (Persona p : lista) {
                System.out.printf("%-5d %-20s %-20s %-15s%n",
                        p.getId_persona(),
                        p.getNombre(),
                        p.getApellido(),
                        p.getTelefono());
            }
        }
    }
    
    // ================= BUSCAR =================
    public static void buscarPersona(int id) {
        System.out.println("\n=== BUSCAR PERSONA ===");
        Persona p = dao.SearchById(id);
        if (p != null) {
            System.out.println("ID: " + p.getId_persona());
            System.out.println("Nombre: " + p.getNombre());
            System.out.println("Apellido: " + p.getApellido());
            System.out.println("Documento: " + p.getDocumento());
            System.out.println("Número: " + p.getNumeroDoc());
            System.out.println("Teléfono: " + p.getTelefono());
        } else {
            System.out.println(" Persona no encontrada");
        }
    }
    
    // ================= ELIMINAR =================
    public static void eliminarPersona(int id) {
        System.out.println("\n=== ELIMINAR PERSONA ===");
        boolean resultado = dao.delete(id);
        if (resultado) {
            System.out.println(" Persona eliminada correctamente");
        } else {
            System.out.println(" Error al eliminar persona");
        }
    }
}
