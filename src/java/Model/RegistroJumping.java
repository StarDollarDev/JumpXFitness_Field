package Model;

import java.sql.Date;

public class RegistroJumping {

    private int id_registro;
    private Cliente cliente;
    private PlanJumping plan;
    private Horario horario;
    private MetodoPago metodoPago;
    private Date fechaIngreso;
    private double monto;

    public RegistroJumping() {
    }

    public RegistroJumping(int id_registro, Cliente cliente, PlanJumping plan, Horario horario, MetodoPago metodoPago, Date fechaIngreso, double monto) {
        this.id_registro = id_registro;
        this.cliente = cliente;
        this.plan = plan;
        this.horario = horario;
        this.metodoPago = metodoPago;
        this.fechaIngreso = fechaIngreso;
        this.monto = monto;
    }

    public int getId_registro() {
        return id_registro;
    }

    public void setId_registro(int id_registro) {
        this.id_registro = id_registro;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public PlanJumping getPlan() {
        return plan;
    }

    public void setPlan(PlanJumping plan) {
        this.plan = plan;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }
    
}
