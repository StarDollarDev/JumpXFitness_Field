package Dao;

import Interface.IDashboard;
import Model.Dashboard;
import Model.Producto;
import Model.RegistroJumping;
import Model.ReservaCancha;
import Model.Venta;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardDaoImpl implements IDashboard {

    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final ReservaCanchaDaoImpl reservaDao = new ReservaCanchaDaoImpl();
    private final VentaDaoImpl ventaDao = new VentaDaoImpl();
    private final RegistroJumpingDaoImpl registroDao = new RegistroJumpingDaoImpl();
    private final ProductoDaoImpl productoDao = new ProductoDaoImpl();

    @Override
    public Dashboard getResumenGeneral() {
        Dashboard dashboard = new Dashboard();
        
        try {
            java.sql.Date hoy = java.sql.Date.valueOf(LocalDate.now());
            
            dashboard.setTotalClientes(clienteDao.lista().size());
            
            List<ReservaCancha> reservasHoy = reservaDao.SearchByFecha(hoy);
            dashboard.setReservasHoy(reservasHoy.size());
            dashboard.setReservasDelDia(reservasHoy);
            
            List<Venta> ventasHoy = ventaDao.SearchByFecha(new java.util.Date());
            double totalVentasHoy = ventasHoy.stream().mapToDouble(Venta::getTotal).sum();
            dashboard.setVentasHoy(totalVentasHoy);
            
            List<RegistroJumping> sesionesHoy = registroDao.getRegistrosByFecha(hoy);
            dashboard.setSesionesJumpingHoy(sesionesHoy.size());
            dashboard.setSesionesDelDia(sesionesHoy);
            
            dashboard.setVentasVariacion(calcularVariacionVentas());
            dashboard.setClientesVariacion(calcularVariacionClientes());
            
            dashboard.setProductosStockBajo(getProductosStockBajo());
            
            dashboard.setVentasProductosSemana(getVentasProductosSemana());
            
            dashboard.setReservasCanchaSemana(getReservasCanchaSemana());
            
            dashboard.setProximasReservas(getProximasReservas());
            
        } catch (Exception e) {
            System.out.println("Error al obtener resumen del dashboard: " + e.getMessage());
        }
        
        return dashboard;
    }

    @Override
    public List<ReservaCancha> getReservasHoy() {
        try {
            java.sql.Date hoy = java.sql.Date.valueOf(LocalDate.now());
            List<ReservaCancha> reservas = reservaDao.SearchByFecha(hoy);
            reservas.sort((r1, r2) -> r1.getHoraInicio().compareTo(r2.getHoraInicio()));
            return reservas;
        } catch (Exception e) {
            System.out.println("Error al obtener reservas de hoy: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RegistroJumping> getSesionesJumpingHoy() {
        try {
            java.sql.Date hoy = java.sql.Date.valueOf(LocalDate.now());
            return registroDao.getRegistrosByFecha(hoy);
        } catch (Exception e) {
            System.out.println("Error al obtener sesiones de hoy: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Producto> getProductosStockBajo() {
        try {
            return productoDao.SearchByStockMinimo(10);
        } catch (Exception e) {
            System.out.println("Error al obtener productos con stock bajo: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Venta> getVentasProductosSemana() {
        try {
            java.util.Date hoy = new java.util.Date();
            long milisegundos = 7 * 24L * 60L * 60L * 1000L;
            java.util.Date fechaInicio = new java.util.Date(hoy.getTime() - milisegundos);
            
            List<Venta> ventas = ventaDao.SearchByDateRange(fechaInicio, hoy);
            
            ventas.sort((v1, v2) -> v2.getFecha().compareTo(v1.getFecha()));
            
            return ventas;
        } catch (Exception e) {
            System.out.println("Error al obtener ventas de productos de la semana: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ReservaCancha> getReservasCanchaSemana() {
        try {
            java.sql.Date hoy = java.sql.Date.valueOf(LocalDate.now());
            java.sql.Date hace7Dias = java.sql.Date.valueOf(LocalDate.now().minusDays(7));
            
            List<ReservaCancha> reservas = new ArrayList<>();
            
            LocalDate fecha = LocalDate.now().minusDays(7);
            while (!fecha.isAfter(LocalDate.now())) {
                java.sql.Date fechaSql = java.sql.Date.valueOf(fecha);
                reservas.addAll(reservaDao.SearchByFecha(fechaSql));
                fecha = fecha.plusDays(1);
            }
            
            reservas.sort((r1, r2) -> r2.getFecha().compareTo(r1.getFecha()));
            
            return reservas;
        } catch (Exception e) {
            System.out.println("Error al obtener reservas de cancha de la semana: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Venta> getVentasUltimosDias(int dias) {
        try {
            java.util.Date hoy = new java.util.Date();
            long milisegundos = dias * 24L * 60L * 60L * 1000L;
            java.util.Date fechaInicio = new java.util.Date(hoy.getTime() - milisegundos);
            return ventaDao.SearchByDateRange(fechaInicio, hoy);
        } catch (Exception e) {
            System.out.println("Error al obtener ventas de los últimos días: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    private List<ReservaCancha> getProximasReservas() {
        try {
            java.sql.Date hoy = java.sql.Date.valueOf(LocalDate.now());
            List<ReservaCancha> reservas = reservaDao.SearchByFecha(hoy);
            
            reservas.removeIf(r -> 
                "FINALIZADA".equals(r.getEstadoReserva().name()) || 
                "CANCELADA".equals(r.getEstadoReserva().name())
            );
            
            reservas.sort((r1, r2) -> r1.getHoraInicio().compareTo(r2.getHoraInicio()));
            
            return reservas.stream().limit(5).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error al obtener próximas reservas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private double calcularVariacionVentas() {
        try {
            java.util.Date inicioMes = getPrimerDiaMes();
            java.util.Date finMes = getUltimoDiaMes();
            List<Venta> ventasMes = ventaDao.SearchByDateRange(inicioMes, finMes);
            double totalMes = ventasMes.stream().mapToDouble(Venta::getTotal).sum();
            
            java.util.Date inicioMesAnterior = getPrimerDiaMesAnterior();
            java.util.Date finMesAnterior = getUltimoDiaMesAnterior();
            List<Venta> ventasMesAnterior = ventaDao.SearchByDateRange(inicioMesAnterior, finMesAnterior);
            double totalMesAnterior = ventasMesAnterior.stream().mapToDouble(Venta::getTotal).sum();
            
            if (totalMesAnterior == 0) {
                return totalMes > 0 ? 100 : 0;
            }
            
            return ((totalMes - totalMesAnterior) / totalMesAnterior) * 100;
        } catch (Exception e) {
            System.out.println("Error al calcular variación de ventas: " + e.getMessage());
            return 0;
        }
    }

    private int calcularVariacionClientes() {
        try {
            int totalClientes = clienteDao.lista().size();
            return (int) (totalClientes * 0.12);
        } catch (Exception e) {
            System.out.println("Error al calcular variación de clientes: " + e.getMessage());
            return 0;
        }
    }

    private java.util.Date getPrimerDiaMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private java.util.Date getUltimoDiaMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    private java.util.Date getPrimerDiaMesAnterior() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private java.util.Date getUltimoDiaMesAnterior() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }
}
