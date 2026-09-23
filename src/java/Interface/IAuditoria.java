package Interface;

import Model.Auditoria;
import java.util.List;

public interface IAuditoria {

    boolean registrar(Auditoria auditoria);

    List<Auditoria> lista();

    List<Auditoria> listaPaginada(int limite);

    List<Auditoria> listarPorTabla(String tabla);

    List<Auditoria> listarPorUsuario(int idUsuario);

    List<Auditoria> listarPorAccion(String accion);

    List<Auditoria> listarPorRangoFecha(String fechaInicio, String fechaFin);
}
