package Dao;

import Interface.IReservaCancha;
import Model.Cliente;
import Model.Deporte;
import Model.EstadoPago;
import Model.EstadoReserva;
import Model.MetodoPago;
import Model.ReservaCancha;
import Util.ConexionSqlSingleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaCanchaDaoImpl implements IReservaCancha {

    private Connection cn;
    private ClienteDaoImpl clienteDAO;

    public ReservaCanchaDaoImpl() {
        this.clienteDAO = new ClienteDaoImpl();
    }

    @Override
    public List<ReservaCancha> lista() {
        List<ReservaCancha> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM reservacancha ORDER BY id_reserva";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                ReservaCancha r = new ReservaCancha();
                r.setId_reserva(rs.getInt("id_reserva"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                r.setDeporte(Deporte.valueOf(rs.getString("deporte")));
                r.setPrecioHora(rs.getDouble("precio_hora"));
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setMontoAdelanto(rs.getDouble("monto_adelanto"));
                r.setFaltaPagar(rs.getDouble("falta_pagar"));
                r.setTotal(rs.getDouble("total"));
                r.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
                r.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estado_reserva")));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al listar reservas de cancha: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(ReservaCancha reserva) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO reservacancha "
                    + "(id_cliente, deporte, precio_hora, metodo_pago, fecha, hora_inicio, hora_fin, "
                    + "monto_adelanto, falta_pagar, total, estado_pago, estado_reserva) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            st = cn.prepareStatement(query, new String[]{"id_reserva"});
            st.setInt(1, reserva.getCliente().getId_cliente());
            st.setString(2, reserva.getDeporte().name());
            st.setDouble(3, reserva.getPrecioHora());
            st.setString(4, reserva.getMetodoPago().name());
            st.setDate(5, reserva.getFecha());
            st.setTime(6, reserva.getHoraInicio());
            st.setTime(7, reserva.getHoraFin());
            st.setDouble(8, reserva.getMontoAdelanto());
            st.setDouble(9, reserva.getFaltaPagar());
            st.setDouble(10, reserva.getTotal());
            st.setString(11, reserva.getEstadoPago().name());
            st.setString(12, reserva.getEstadoReserva().name());

            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    reserva.setId_reserva(rs.getInt(1));
                }
                System.out.println("Reserva de cancha registrada correctamente con ID: " + reserva.getId_reserva());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar reserva de cancha: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public boolean update(ReservaCancha reserva) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "UPDATE reservacancha SET id_cliente=?, deporte=?, "
                    + "precio_hora=?, metodo_pago=?, fecha=?, "
                    + "hora_inicio=?, hora_fin=?, monto_adelanto=?, "
                    + "falta_pagar=?, total=?, estado_pago=?, estado_reserva=? "
                    + "WHERE id_reserva=?";
            st = cn.prepareStatement(query);
            st.setInt(1, reserva.getCliente().getId_cliente());
            st.setString(2, reserva.getDeporte().name());
            st.setDouble(3, reserva.getPrecioHora());
            st.setString(4, reserva.getMetodoPago().name());
            st.setDate(5, reserva.getFecha());
            st.setTime(6, reserva.getHoraInicio());
            st.setTime(7, reserva.getHoraFin());
            st.setDouble(8, reserva.getMontoAdelanto());
            st.setDouble(9, reserva.getFaltaPagar());
            st.setDouble(10, reserva.getTotal());
            st.setString(11, reserva.getEstadoPago().name());
            st.setString(12, reserva.getEstadoReserva().name());
            st.setInt(13, reserva.getId_reserva());

            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar reserva de cancha: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public ReservaCancha SearchById(int id) {
        ReservaCancha reserva = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM reservacancha WHERE id_reserva = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                reserva = new ReservaCancha();
                reserva.setId_reserva(rs.getInt("id_reserva"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                reserva.setCliente(c);
                reserva.setDeporte(Deporte.valueOf(rs.getString("deporte")));
                reserva.setPrecioHora(rs.getDouble("precio_hora"));
                reserva.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                reserva.setFecha(rs.getDate("fecha"));
                reserva.setHoraInicio(rs.getTime("hora_inicio"));
                reserva.setHoraFin(rs.getTime("hora_fin"));
                reserva.setMontoAdelanto(rs.getDouble("monto_adelanto"));
                reserva.setFaltaPagar(rs.getDouble("falta_pagar"));
                reserva.setTotal(rs.getDouble("total"));
                reserva.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
                reserva.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estado_reserva")));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar reserva de cancha: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return reserva;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "DELETE FROM reservacancha WHERE id_reserva = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar reserva de cancha: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public List<ReservaCancha> SearchByFecha(Date fecha) {
        List<ReservaCancha> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM reservacancha WHERE fecha=? ORDER BY hora_inicio";
            st = cn.prepareStatement(query);
            st.setDate(1, fecha);
            rs = st.executeQuery();
            while (rs.next()) {
                ReservaCancha r = new ReservaCancha();
                r.setId_reserva(rs.getInt("id_reserva"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                r.setDeporte(Deporte.valueOf(rs.getString("deporte")));
                r.setPrecioHora(rs.getDouble("precio_hora"));
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setMontoAdelanto(rs.getDouble("monto_adelanto"));
                r.setFaltaPagar(rs.getDouble("falta_pagar"));
                r.setTotal(rs.getDouble("total"));
                r.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
                r.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estado_reserva")));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar reservas por fecha: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<ReservaCancha> SearchByClienteId(int idCliente) {
        List<ReservaCancha> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM reservacancha WHERE id_cliente = ? ORDER BY fecha DESC, hora_inicio";
            st = cn.prepareStatement(query);
            st.setInt(1, idCliente);
            rs = st.executeQuery();
            while (rs.next()) {
                ReservaCancha r = new ReservaCancha();
                r.setId_reserva(rs.getInt("id_reserva"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                r.setDeporte(Deporte.valueOf(rs.getString("deporte")));
                r.setPrecioHora(rs.getDouble("precio_hora"));
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setMontoAdelanto(rs.getDouble("monto_adelanto"));
                r.setFaltaPagar(rs.getDouble("falta_pagar"));
                r.setTotal(rs.getDouble("total"));
                r.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
                r.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estado_reserva")));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar reservas por cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<ReservaCancha> SearchByEstadoReserva(String estado) {
        List<ReservaCancha> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM reservacancha WHERE estado_reserva = ? ORDER BY fecha DESC, hora_inicio";
            st = cn.prepareStatement(query);
            st.setString(1, estado);
            rs = st.executeQuery();
            while (rs.next()) {
                ReservaCancha r = new ReservaCancha();
                r.setId_reserva(rs.getInt("id_reserva"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                r.setDeporte(Deporte.valueOf(rs.getString("deporte")));
                r.setPrecioHora(rs.getDouble("precio_hora"));
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setMontoAdelanto(rs.getDouble("monto_adelanto"));
                r.setFaltaPagar(rs.getDouble("falta_pagar"));
                r.setTotal(rs.getDouble("total"));
                r.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
                r.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estado_reserva")));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar reservas por estado: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<ReservaCancha> SearchByDeporte(String deporte) {
        List<ReservaCancha> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM reservacancha WHERE deporte = ? ORDER BY fecha DESC, hora_inicio";
            st = cn.prepareStatement(query);
            st.setString(1, deporte);
            rs = st.executeQuery();
            while (rs.next()) {
                ReservaCancha r = new ReservaCancha();
                r.setId_reserva(rs.getInt("id_reserva"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                r.setDeporte(Deporte.valueOf(rs.getString("deporte")));
                r.setPrecioHora(rs.getDouble("precio_hora"));
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setMontoAdelanto(rs.getDouble("monto_adelanto"));
                r.setFaltaPagar(rs.getDouble("falta_pagar"));
                r.setTotal(rs.getDouble("total"));
                r.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
                r.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estado_reserva")));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar reservas por deporte: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<ReservaCancha> SearchReservasActivas() {
        List<ReservaCancha> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM reservacancha WHERE estado_reserva IN ('CONFIRMADO', 'EN_CURSO', 'PAGADA') ORDER BY fecha ASC, hora_inicio";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                ReservaCancha r = new ReservaCancha();
                r.setId_reserva(rs.getInt("id_reserva"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                r.setDeporte(Deporte.valueOf(rs.getString("deporte")));
                r.setPrecioHora(rs.getDouble("precio_hora"));
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setMontoAdelanto(rs.getDouble("monto_adelanto"));
                r.setFaltaPagar(rs.getDouble("falta_pagar"));
                r.setTotal(rs.getDouble("total"));
                r.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
                r.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estado_reserva")));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar reservas activas: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    private void cerrarRecursos(ResultSet rs, PreparedStatement st) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
        } catch (Exception ex) {
            System.out.println("Error cerrando recursos: " + ex.getMessage());
        }
    }

}
