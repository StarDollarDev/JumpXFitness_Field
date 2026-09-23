
package Interface;

import Model.Cliente;
import java.util.List;

public interface ICliente {
    
    public List<Cliente> lista();
    public boolean insertar(Cliente cliente);
    public boolean update(Cliente cliente);
    public Cliente SearchById(int id);
    public boolean delete(int id);
    public Cliente SearchByPersonaId(int idPersona);

    // Crea la fila de cliente para una persona que YA existe
    // (evita duplicar la persona cuando el cliente vuelve).
    public Cliente crearParaPersonaExistente(int idPersona);
    
}
