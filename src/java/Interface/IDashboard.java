
package Interface;

import Model.Dashboard;
import Model.Producto;
import Model.RegistroJumping;
import Model.ReservaCancha;
import Model.Venta;
import java.util.List;

public interface IDashboard {
    public Dashboard getResumenGeneral();
    public List<ReservaCancha> getReservasHoy();
    public List<RegistroJumping> getSesionesJumpingHoy();
    public List<Producto> getProductosStockBajo();
    
    public List<Venta> getVentasProductosSemana();
    public List<ReservaCancha> getReservasCanchaSemana();
    public List<Venta> getVentasUltimosDias(int dias);
}
