package Interface;

import Model.PagoCulqi;
import java.util.List;

public interface IPagoCulqi {
    boolean crear(PagoCulqi pago);
    boolean actualizarPorCargo(String culqiChargeId, String estado, String respuestaJson);
    boolean actualizarPorOrden(String culqiOrderId, String estado, String respuestaJson);
    boolean marcarConfirmadoYExtenderPlan(int idPago);
    PagoCulqi SearchById(int idPago);
    PagoCulqi SearchByOrderId(String culqiOrderId);
    List<PagoCulqi> listaPorCliente(int idCliente);
    List<PagoCulqi> listaTodos();
}
