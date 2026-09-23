package Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Registro del Libro de Reclamaciones virtual (formato exigido por Indecopi,
 * D.S. N° 011-2011-PCM / D.S. N° 101-2022-PCM). idReclamo y codigo se
 * generan al insertar; el resto lo llena el consumidor en el formulario.
 */
public class Reclamo {

    private int idReclamo;
    private String codigo;

    // Datos del consumidor reclamante
    private String nombreCompleto;
    private String tipoDoc;      // DNI | CE
    private String numeroDoc;
    private String direccion;
    private String telefono;
    private String correo;
    private String apoderado;    // opcional: si el consumidor es menor de edad

    // Detalle del bien contratado
    private String tipoBien;         // PRODUCTO | SERVICIO
    private String descripcionBien;
    private BigDecimal monto;

    // Detalle del reclamo/queja
    private String tipoReclamo;  // RECLAMO | QUEJA
    private String detalle;
    private String pedido;       // qué solicita el consumidor (opcional)

    private String estado;       // REGISTRADO | EN_PROCESO | RESUELTO
    private Timestamp fechaRegistro;

    public Reclamo() {
    }

    public int getIdReclamo() { return idReclamo; }
    public void setIdReclamo(int idReclamo) { this.idReclamo = idReclamo; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTipoDoc() { return tipoDoc; }
    public void setTipoDoc(String tipoDoc) { this.tipoDoc = tipoDoc; }
    public String getNumeroDoc() { return numeroDoc; }
    public void setNumeroDoc(String numeroDoc) { this.numeroDoc = numeroDoc; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getApoderado() { return apoderado; }
    public void setApoderado(String apoderado) { this.apoderado = apoderado; }
    public String getTipoBien() { return tipoBien; }
    public void setTipoBien(String tipoBien) { this.tipoBien = tipoBien; }
    public String getDescripcionBien() { return descripcionBien; }
    public void setDescripcionBien(String descripcionBien) { this.descripcionBien = descripcionBien; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getTipoReclamo() { return tipoReclamo; }
    public void setTipoReclamo(String tipoReclamo) { this.tipoReclamo = tipoReclamo; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getPedido() { return pedido; }
    public void setPedido(String pedido) { this.pedido = pedido; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
