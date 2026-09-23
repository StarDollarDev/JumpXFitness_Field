
package Interface;

import Model.Venta;
import java.util.List;
import java.util.Date;

public interface IVenta {
    
    public List<Venta> lista();
    public boolean insertar(Venta venta);
    public boolean update(Venta venta);
    public Venta SearchById(int id);
    public boolean delete(int id);
    public List<Venta> SearchByDateRange(Date inicio, Date fin);
    public List<Venta> SearchByFecha(Date fecha);
    public List<Venta> SearchByClienteId(int idCliente);
}
