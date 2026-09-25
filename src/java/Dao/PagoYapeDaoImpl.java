package Dao;

import Interface.IPagoYape;
import Model.Cliente;
import Model.PagoYape;
import Model.PlanJumping;
import Util.ConexionSqlSingleton;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoYapeDaoImpl implements IPagoYape {

    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final PlanJumpingDaoImpl planDao = new PlanJumpingDaoImpl();

    @Override
    public boolean solicitar(PagoYape p) {
        String sql = "INSERT INTO pago_yape_jx "
                + "(id_cliente, id_plan, monto, celular_pagador, codigo_operacion, estado) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDIENTE')";
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql, new String[]{"id_pago"})) {
            st.setInt(1, p.getCliente().getId_cliente());
            st.setInt(2, p.getPlan().getId_plan());
            st.setDouble(3, p.getMonto());
            st.setString(4, p.getCelularPagador());
            st.setString(5, p.getCodigoOperacion());

            int r = st.executeUpdate();
            if (r == 0) {
                return false;
            }
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIdPago(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al solicitar pago Yape <<<");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<PagoYape> listaPendientes() {
        return listarPorFiltro("WHERE estado = 'PENDIENTE' ORDER BY fecha_solicitud ASC");
    }

    @Override
    public List<PagoYape> listaPorCliente(int idCliente) {
        return listarPorFiltro("WHERE id_cliente = " + idCliente + " ORDER BY fecha_solicitud DESC");
    }

    private List<PagoYape> listarPorFiltro(String whereOrderBy) {
        List<PagoYape> lista = new ArrayList<>();
        String sql = "SELECT * FROM pago_yape_jx " + whereOrderBy;
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al listar pagos Yape <<<");
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public PagoYape SearchById(int idPago) {
        String sql = "SELECT * FROM pago_yape_jx WHERE id_pago = ?";
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            st.setInt(1, idPago);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al buscar pago Yape <<<");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Confirma el pago: crea un registrojumping nuevo (sin horario, es una
     * renovación de plan y no una reserva de clase puntual) que es el que
     * hace avanzar la vigencia del cliente, y marca el pago como CONFIRMADO.
     * Todo en una sola transacción: si algo falla, no se confirma a medias.
     */
    @Override
    public boolean confirmar(int idPago) {
        String sqlRegistro = "INSERT INTO registrojumping (id_cliente, id_plan, id_horario, metodo_pago, fecha_ingreso, monto) "
                + "VALUES (?, ?, NULL, 'YAPE', SYSDATE, ?)";
        String sqlPago = "UPDATE pago_yape_jx SET estado = 'CONFIRMADO', fecha_confirmacion = SYSTIMESTAMP, "
                + "id_registro_generado = ? WHERE id_pago = ? AND estado = 'PENDIENTE'";

        PagoYape pago = SearchById(idPago);
        if (pago == null || !"PENDIENTE".equals(pago.getEstado())) {
            return false;
        }

        try (Connection cn = ConexionSqlSingleton.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int idRegistro;
                try (PreparedStatement st = cn.prepareStatement(sqlRegistro, new String[]{"id_registro"})) {
                    st.setInt(1, pago.getCliente().getId_cliente());
                    st.setInt(2, pago.getPlan().getId_plan());
                    st.setDouble(3, pago.getMonto());
                    st.executeUpdate();
                    try (ResultSet rs = st.getGeneratedKeys()) {
                        rs.next();
                        idRegistro = rs.getInt(1);
                    }
                }
                try (PreparedStatement up = cn.prepareStatement(sqlPago)) {
                    up.setInt(1, idRegistro);
                    up.setInt(2, idPago);
                    int filas = up.executeUpdate();
                    if (filas == 0) {
                        cn.rollback();
                        return false;
                    }
                }
                cn.commit();
                return true;
            } catch (SQLException e) {
                cn.rollback();
                System.err.println(">>> ERROR SQL al confirmar pago Yape (se revirtió todo) <<<");
                e.printStackTrace();
                return false;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL de conexión al confirmar pago Yape <<<");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rechazar(int idPago, String motivo) {
        String sql = "UPDATE pago_yape_jx SET estado = 'RECHAZADO', fecha_confirmacion = SYSTIMESTAMP, "
                + "motivo_rechazo = ? WHERE id_pago = ? AND estado = 'PENDIENTE'";
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            st.setString(1, motivo);
            st.setInt(2, idPago);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al rechazar pago Yape <<<");
            e.printStackTrace();
            return false;
        }
    }

    private PagoYape mapear(ResultSet rs) throws SQLException {
        PagoYape p = new PagoYape();
        p.setIdPago(rs.getInt("id_pago"));
        Cliente c = clienteDao.SearchById(rs.getInt("id_cliente"));
        p.setCliente(c);
        PlanJumping plan = planDao.SearchById(rs.getInt("id_plan"));
        p.setPlan(plan);
        p.setMonto(rs.getDouble("monto"));
        p.setCelularPagador(rs.getString("celular_pagador"));
        p.setCodigoOperacion(rs.getString("codigo_operacion"));
        p.setEstado(rs.getString("estado"));
        p.setFechaSolicitud(rs.getTimestamp("fecha_solicitud"));
        p.setFechaConfirmacion(rs.getTimestamp("fecha_confirmacion"));
        int idRegistro = rs.getInt("id_registro_generado");
        p.setIdRegistroGenerado(rs.wasNull() ? null : idRegistro);
        p.setMotivoRechazo(rs.getString("motivo_rechazo"));
        return p;
    }
}
