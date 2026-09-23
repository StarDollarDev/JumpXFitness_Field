package Interface;

import Model.Reclamo;
import java.util.List;

public interface IReclamo {
    boolean insertar(Reclamo reclamo);
    List<Reclamo> lista();
    Reclamo buscarPorCodigo(String codigo);
}
