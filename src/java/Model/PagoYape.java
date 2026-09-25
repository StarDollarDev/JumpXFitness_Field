package Model;

import java.sql.Timestamp;

/**
 * Solicitud de pago por Yape (renovación o compra de plan) hecha por un
 * cliente desde su panel. No hay verificación automática con Yape (no existe
 * una API pública simple para eso): el cliente escribe el código de
 * operación que le muestra su app y un administrador la confirma o rechaza
 * a mano, viendo su propia app de Yape.
 */
public class PagoYape {

    private int idPago;
    private Cliente cliente;
    private PlanJumping plan;
    private double monto;
    private String celularPagador;
    private String codigoOperacion;
    private String estado; // PENDIENTE | CONFIRMADO | RECHAZADO
    private Timestamp fechaSolicitud;
    private Timestamp fechaConfirmacion;
    private Integer idRegistroGenerado; // id_registro creado al confirmar (o null)
    private String motivoRechazo;

    public PagoYape() {
    }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public PlanJumping getPlan() { return plan; }
    public void setPlan(PlanJumping plan) { this.plan = plan; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getCelularPagador() { return celularPagador; }
    public void setCelularPagador(String celularPagador) { this.celularPagador = celularPagador; }
    public String getCodigoOperacion() { return codigoOperacion; }
    public void setCodigoOperacion(String codigoOperacion) { this.codigoOperacion = codigoOperacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Timestamp getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Timestamp fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public Timestamp getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(Timestamp fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }
    public Integer getIdRegistroGenerado() { return idRegistroGenerado; }
    public void setIdRegistroGenerado(Integer idRegistroGenerado) { this.idRegistroGenerado = idRegistroGenerado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}
