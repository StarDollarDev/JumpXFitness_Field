package Model;

import java.sql.Timestamp;

public class Auditoria {

    private int id_auditoria;
    private Integer id_usuario;      // puede ser null si el usuario fue eliminado
    private String usuarioNombre;    // snapshot del nombre de usuario al momento del evento
    private String accion;           // LOGIN, LOGOUT, LOGIN_FALLIDO, INSERT, UPDATE, DELETE
    private String tablaAfectada;    // ej: producto, reservacancha, usuario
    private String idRegistroAfectado; // PK del registro afectado, como texto
    private String detalle;          // descripción legible del cambio
    private String ipOrigen;
    private Timestamp fechaHora;

    public Auditoria() {
    }

    public Auditoria(int id_auditoria, Integer id_usuario, String usuarioNombre, String accion,
            String tablaAfectada, String idRegistroAfectado, String detalle, String ipOrigen, Timestamp fechaHora) {
        this.id_auditoria = id_auditoria;
        this.id_usuario = id_usuario;
        this.usuarioNombre = usuarioNombre;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.idRegistroAfectado = idRegistroAfectado;
        this.detalle = detalle;
        this.ipOrigen = ipOrigen;
        this.fechaHora = fechaHora;
    }

    public int getId_auditoria() {
        return id_auditoria;
    }

    public void setId_auditoria(int id_auditoria) {
        this.id_auditoria = id_auditoria;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public String getIdRegistroAfectado() {
        return idRegistroAfectado;
    }

    public void setIdRegistroAfectado(String idRegistroAfectado) {
        this.idRegistroAfectado = idRegistroAfectado;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public Timestamp getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Timestamp fechaHora) {
        this.fechaHora = fechaHora;
    }
}
