
package Interface;

import Model.Horario;
import Model.RegistroJumping;
import java.util.List;

public interface IRegistroJumping {
    
    public List<RegistroJumping> lista();
    public boolean insertar(RegistroJumping registro);
    public boolean update(RegistroJumping registro);
    public RegistroJumping SearchById(int id);
    public boolean delete(int id);
    public List<RegistroJumping> getRegistrosByFecha(java.sql.Date fecha);
    public List<Horario> getHorariosDisponibles(java.sql.Date fecha);
    public boolean isHorarioOcupado(int idHorario, java.sql.Date fecha);
    public List<Horario> getHorariosOcupados(java.sql.Date fecha);
    public List<RegistroJumping> SearchByClienteId(int idCliente);
}
