package Model;

import java.util.Date;

public class Venta {

    private int id_venta;
    // Puede ser null si es venta anónima
    private Cliente cliente;
    private MetodoPago metodoPago;
    private Date fecha;
    private double total;

    public Venta() {
    }
    
    

    public Venta(int id_venta, Cliente cliente, MetodoPago metodoPago, Date fecha, double total) {
        this.id_venta = id_venta;
        this.cliente = cliente;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.total = total;
    }

    public int getId_venta() {
        return id_venta;
    }

    public void setId_venta(int id_venta) {
        this.id_venta = id_venta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    
}
