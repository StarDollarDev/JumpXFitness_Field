package Util;

import Dao.AuditoriaDaoImpl;
import Interface.IAuditoria;
import Model.Auditoria;
import Model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Punto único para dejar constancia en la tabla AUDITORIA de toda operación
 * relevante (INSERT/UPDATE/DELETE/LOGIN/LOGOUT). Se apoya en la sesión HTTP
 * para saber qué administrador realizó la acción.
 *
 * Nunca lanza excepción hacia el controller que la invoca: un fallo al
 * auditar no debe impedir que la operación de negocio se complete.
 */
public class AuditoriaHelper {

    private static final IAuditoria dao = new AuditoriaDaoImpl();

    private AuditoriaHelper() {
    }

    public static void registrar(HttpServletRequest request, String accion, String tabla,
            Object idRegistro, String detalle) {
        try {
            Auditoria a = new Auditoria();
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("usuario") != null) {
                Usuario u = (Usuario) session.getAttribute("usuario");
                a.setId_usuario(u.getId_usuario());
                a.setUsuarioNombre(u.getUsuario());
            } else {
                a.setId_usuario(null);
                a.setUsuarioNombre("SISTEMA");
            }
            a.setAccion(accion);
            a.setTablaAfectada(tabla);
            a.setIdRegistroAfectado(idRegistro != null ? String.valueOf(idRegistro) : null);
            a.setDetalle(detalle);
            a.setIpOrigen(request.getRemoteAddr());
            dao.registrar(a);
        } catch (Exception e) {
            System.out.println("No se pudo registrar auditoria: " + e.getMessage());
        }
    }

    /** Variante para el login, donde todavía no se ha guardado el "usuario" en sesión. */
    public static void registrarConUsuario(HttpServletRequest request, Integer idUsuario, String nombreUsuario,
            String accion, String tabla, Object idRegistro, String detalle) {
        try {
            Auditoria a = new Auditoria();
            a.setId_usuario(idUsuario);
            a.setUsuarioNombre(nombreUsuario);
            a.setAccion(accion);
            a.setTablaAfectada(tabla);
            a.setIdRegistroAfectado(idRegistro != null ? String.valueOf(idRegistro) : null);
            a.setDetalle(detalle);
            a.setIpOrigen(request.getRemoteAddr());
            dao.registrar(a);
        } catch (Exception e) {
            System.out.println("No se pudo registrar auditoria: " + e.getMessage());
        }
    }
}
