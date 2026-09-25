package Interface;

import Model.PagoYape;
import java.util.List;

public interface IPagoYape {
    boolean solicitar(PagoYape pago);
    List<PagoYape> listaPendientes();
    List<PagoYape> listaPorCliente(int idCliente);
    PagoYape SearchById(int idPago);
    boolean confirmar(int idPago);
    boolean rechazar(int idPago, String motivo);
}
