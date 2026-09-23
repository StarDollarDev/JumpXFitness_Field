
package Model;

import java.sql.Date;
import java.sql.Time;

public class ReservaCancha {
    
    private int id_reserva;
    private Cliente cliente;
    private Deporte deporte;
    private double precioHora;
    private MetodoPago metodoPago;
    private Date fecha;
    private Time horaInicio;
    private Time horaFin;
    private double montoAdelanto;
    private double faltaPagar;
    private double total;
    private EstadoPago estadoPago;
    private EstadoReserva estadoReserva;

    public ReservaCancha() {
    }

    public ReservaCancha(int id_reserva, Cliente cliente, Deporte deporte, double precioHora, MetodoPago metodoPago, Date fecha, Time horaInicio, Time horaFin, double montoAdelanto, double faltaPagar, double total, EstadoPago estadoPago, EstadoReserva estadoReserva) {
        this.id_reserva = id_reserva;
        this.cliente = cliente;
        this.deporte = deporte;
        this.precioHora = precioHora;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.montoAdelanto = montoAdelanto;
        this.faltaPagar = faltaPagar;
        this.total = total;
        this.estadoPago = estadoPago;
        this.estadoReserva = estadoReserva;
    }

    public int getId_reserva() {
        return id_reserva;
    }

    public void setId_reserva(int id_reserva) {
        this.id_reserva = id_reserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }

    public double getPrecioHora() {
        return precioHora;
    }

    public void setPrecioHora(double precioHora) {
        this.precioHora = precioHora;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Time horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Time getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Time horaFin) {
        this.horaFin = horaFin;
    }

    public double getMontoAdelanto() {
        return montoAdelanto;
    }

    public void setMontoAdelanto(double montoAdelanto) {
        this.montoAdelanto = montoAdelanto;
    }

    public double getFaltaPagar() {
        return faltaPagar;
    }

    public void setFaltaPagar(double faltaPagar) {
        this.faltaPagar = faltaPagar;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public EstadoReserva getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(EstadoReserva estadoReserva) {
        this.estadoReserva = estadoReserva;
    }
    
    
}
