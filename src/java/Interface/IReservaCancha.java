
package Interface;

import Model.ReservaCancha;
import java.util.List;
import java.sql.Date;

public interface IReservaCancha {
    
    public List<ReservaCancha> lista();
    public boolean insertar(ReservaCancha reserva);
    public boolean update(ReservaCancha reserva);
    public ReservaCancha SearchById(int id);
    public boolean delete(int id);
    public List<ReservaCancha> SearchByFecha(Date fecha);
    public List<ReservaCancha> SearchByEstadoReserva(String estado);
    public List<ReservaCancha> SearchByDeporte(String deporte);
    public List<ReservaCancha> SearchReservasActivas();
    public List<ReservaCancha> SearchByClienteId(int idCliente);
}
