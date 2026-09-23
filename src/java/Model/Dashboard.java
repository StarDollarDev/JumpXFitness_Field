
package Model;

import java.util.List;

public class Dashboard {
    private int totalClientes;
    private int reservasHoy;
    private double ventasHoy;
    private int sesionesJumpingHoy;
    private double ventasVariacion; 
    private int clientesVariacion;
    private List<ReservaCancha> reservasDelDia;
    private List<RegistroJumping> sesionesDelDia;
    private List<Producto> productosStockBajo;
    private List<Venta> ventasProductosSemana;
    private List<ReservaCancha> reservasCanchaSemana;
    private List<ReservaCancha> proximasReservas;

    public Dashboard() {
    }

    public Dashboard(int totalClientes, int reservasHoy, double ventasHoy, int sesionesJumpingHoy, double ventasVariacion, int clientesVariacion, List<ReservaCancha> reservasDelDia, List<RegistroJumping> sesionesDelDia, List<Producto> productosStockBajo, List<Venta> ventasProductosSemana, List<ReservaCancha> reservasCanchaSemana, List<ReservaCancha> proximasReservas) {
        this.totalClientes = totalClientes;
        this.reservasHoy = reservasHoy;
        this.ventasHoy = ventasHoy;
        this.sesionesJumpingHoy = sesionesJumpingHoy;
        this.ventasVariacion = ventasVariacion;
        this.clientesVariacion = clientesVariacion;
        this.reservasDelDia = reservasDelDia;
        this.sesionesDelDia = sesionesDelDia;
        this.productosStockBajo = productosStockBajo;
        this.ventasProductosSemana = ventasProductosSemana;
        this.reservasCanchaSemana = reservasCanchaSemana;
        this.proximasReservas = proximasReservas;
    }


    public int getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public int getReservasHoy() {
        return reservasHoy;
    }

    public void setReservasHoy(int reservasHoy) {
        this.reservasHoy = reservasHoy;
    }

    public double getVentasHoy() {
        return ventasHoy;
    }

    public void setVentasHoy(double ventasHoy) {
        this.ventasHoy = ventasHoy;
    }

    public int getSesionesJumpingHoy() {
        return sesionesJumpingHoy;
    }

    public void setSesionesJumpingHoy(int sesionesJumpingHoy) {
        this.sesionesJumpingHoy = sesionesJumpingHoy;
    }

    public double getVentasVariacion() {
        return ventasVariacion;
    }

    public void setVentasVariacion(double ventasVariacion) {
        this.ventasVariacion = ventasVariacion;
    }

    public int getClientesVariacion() {
        return clientesVariacion;
    }

    public void setClientesVariacion(int clientesVariacion) {
        this.clientesVariacion = clientesVariacion;
    }

    public List<ReservaCancha> getReservasDelDia() {
        return reservasDelDia;
    }

    public void setReservasDelDia(List<ReservaCancha> reservasDelDia) {
        this.reservasDelDia = reservasDelDia;
    }

    public List<RegistroJumping> getSesionesDelDia() {
        return sesionesDelDia;
    }

    public void setSesionesDelDia(List<RegistroJumping> sesionesDelDia) {
        this.sesionesDelDia = sesionesDelDia;
    }

    public List<Producto> getProductosStockBajo() {
        return productosStockBajo;
    }

    public void setProductosStockBajo(List<Producto> productosStockBajo) {
        this.productosStockBajo = productosStockBajo;
    }

    public List<Venta> getVentasProductosSemana() {
        return ventasProductosSemana;
    }

    public void setVentasProductosSemana(List<Venta> ventasProductosSemana) {
        this.ventasProductosSemana = ventasProductosSemana;
    }

    public List<ReservaCancha> getReservasCanchaSemana() {
        return reservasCanchaSemana;
    }

    public void setReservasCanchaSemana(List<ReservaCancha> reservasCanchaSemana) {
        this.reservasCanchaSemana = reservasCanchaSemana;
    }


    public List<ReservaCancha> getProximasReservas() {
        return proximasReservas;
    }

    public void setProximasReservas(List<ReservaCancha> proximasReservas) {
        this.proximasReservas = proximasReservas;
    }       
}
