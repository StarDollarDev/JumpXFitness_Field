
package Interface;

import Model.Persona;
import Model.Usuario;
import java.util.List;

public interface IPersona {
    public List<Persona> lista();
    public boolean insertSoloPersona(Persona p, Usuario u);
    public boolean update(Persona p);
    public Persona SearchById(int id);
    public boolean delete(int id);
    public Persona SearchByDocumento(String documento, String numeroDoc);
    public boolean existeNumeroDoc(String numeroDoc);
    public boolean existeTelefono(String telefono);
}
