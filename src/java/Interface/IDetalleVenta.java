
package Interface;

import Model.DetalleVenta;
import java.util.List;

public interface IDetalleVenta {
    
    public List<DetalleVenta> lista();
    public boolean insertar(DetalleVenta detalle);
    public boolean update(DetalleVenta detalle);
    public DetalleVenta SearchById(int id);
    public boolean delete(int id);
    public List<DetalleVenta> SearchByVentaId(int idVenta);
    public List<DetalleVenta> SearchByClienteId(int idCliente);
    
}
