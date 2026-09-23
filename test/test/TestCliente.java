
package test;

import Dao.ClienteDaoImpl;
import Model.Cliente;
import Model.Persona;

public class TestCliente {
    
    static ClienteDaoImpl dao = new ClienteDaoImpl();

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println(" TEST CRUD CLIENTE");
        System.out.println("====================================");
        
        insertarCliente();
        //listarClientes();
        //buscarCliente(1);
        //actualizarCliente(1);
        //eliminarCliente(1);
    }
    
    // ================= INSERT =================
    public static void insertarCliente() {
        System.out.println("\n=== INSERTAR CLIENTE ===");
        Cliente cliente = new Cliente();
        
        Persona persona = new Persona();
        persona.setNombre("Juan");
        persona.setApellido("Perez");
        persona.setDocumento("DNI");
        persona.setNumeroDoc("12345678");
        persona.setTelefono("987654321");
        
        cliente.setPersona(persona);

        boolean resultado = dao.insertar(cliente);
        if (resultado) {
            System.out.println(" Cliente insertado correctamente con ID: " + cliente.getId_cliente());
            System.out.println(" Persona asociada ID: " + cliente.getPersona().getId_persona());
        } else {
            System.out.println(" Error al insertar cliente");
        }
    }
    
    // ================= LISTAR =================
    public static void listarClientes() {
        System.out.println("\n=== LISTA DE CLIENTES ===");
        var lista = dao.lista();
        if (lista.isEmpty()) {
            System.out.println("No existen registros");
        } else {
            System.out.printf("%-5s %-20s %-20s %-15s%n", "ID", "NOMBRE", "APELLIDO", "TELEFONO");
            System.out.println("--------------------------------------------------------------");
            for (Cliente c : lista) {
                Persona p = c.getPersona();
                System.out.printf("%-5d %-20s %-20s %-15s%n",
                        c.getId_cliente(),
                        p != null ? p.getNombre() : "",
                        p != null ? p.getApellido() : "",
                        p != null ? p.getTelefono() : "");
            }
        }
    }
    
    // ================= BUSCAR =================
    public static void buscarCliente(int id) {
        System.out.println("\n=== BUSCAR CLIENTE ===");
        Cliente c = dao.SearchById(id);
        if (c != null) {
            Persona p = c.getPersona();
            System.out.println("ID Cliente: " + c.getId_cliente());
            System.out.println("ID Persona: " + p.getId_persona());
            System.out.println("Nombre: " + p.getNombre());
            System.out.println("Apellido: " + p.getApellido());
            System.out.println("Documento: " + p.getDocumento());
            System.out.println("Número: " + p.getNumeroDoc());
            System.out.println("Teléfono: " + p.getTelefono());
        } else {
            System.out.println(" Cliente no encontrado");
        }
    }
    // ================= ACTUALIZAR =================
    public static void actualizarCliente(int id) {
        System.out.println("\n=== ACTUALIZAR CLIENTE ===");
        Cliente c = dao.SearchById(id);
        if (c != null) {
            Persona p = c.getPersona();
            p.setNombre("Juan Carlos");
            p.setTelefono("999888777");
            
            boolean resultado = dao.update(c);
            if (resultado) {
                System.out.println(" Cliente actualizado correctamente");
            } else {
                System.out.println(" Error al actualizar cliente");
            }
        } else {
            System.out.println(" Cliente no encontrado");
        }
    }
    // ================= ELIMINAR =================
    public static void eliminarCliente(int id) {
        System.out.println("\n=== ELIMINAR CLIENTE ===");
        boolean resultado = dao.delete(id);
        if (resultado) {
            System.out.println(" Cliente eliminado correctamente");
        } else {
            System.out.println(" Error al eliminar cliente");
        }
    }
}
