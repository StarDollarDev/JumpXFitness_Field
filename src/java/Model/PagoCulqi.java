package Model;

import java.sql.Timestamp;

/**
 * Pago procesado por Culqi: tarjeta o Yape (confirmación inmediata, vía
 * /v2/charges) o PagoEfectivo (confirmación diferida por webhook, vía
 * /v2/orders).
 */
public class PagoCulqi {

    private int idPago;
    private Cliente cliente;
    private PlanJumping plan;
    private String metodo;   // TARJETA | YAPE | PAGOEFECTIVO
    private double monto;
    private String moneda;   // PEN
    private String culqiChargeId;
    private String culqiOrderId;
    private String estado;   // PENDIENTE | PAGADO | RECHAZADO | EXPIRADO
    private String respuestaJson; // respuesta cruda de Culqi, para depurar
    private Timestamp fechaCreacion;
    private Timestamp fechaConfirmacion;
    private Integer idRegistroGenerado;

    public PagoCulqi() {
    }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public PlanJumping getPlan() { return plan; }
    public void setPlan(PlanJumping plan) { this.plan = plan; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getCulqiChargeId() { return culqiChargeId; }
    public void setCulqiChargeId(String culqiChargeId) { this.culqiChargeId = culqiChargeId; }
    public String getCulqiOrderId() { return culqiOrderId; }
    public void setCulqiOrderId(String culqiOrderId) { this.culqiOrderId = culqiOrderId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getRespuestaJson() { return respuestaJson; }
    public void setRespuestaJson(String respuestaJson) { this.respuestaJson = respuestaJson; }
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Timestamp getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(Timestamp fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }
    public Integer getIdRegistroGenerado() { return idRegistroGenerado; }
    public void setIdRegistroGenerado(Integer idRegistroGenerado) { this.idRegistroGenerado = idRegistroGenerado; }
}
