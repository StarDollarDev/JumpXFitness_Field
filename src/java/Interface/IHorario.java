
package Interface;

import Model.Horario;
import java.util.List;

public interface IHorario {
    
    public List<Horario> lista();
    public boolean insertar(Horario horario);
    public boolean update(Horario horario);
    public Horario SearchById(int id);
    public boolean delete(int id);
    public List<Horario> listaActivos();
}
