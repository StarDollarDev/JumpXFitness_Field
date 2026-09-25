package Dao;

import Interface.IPagoCulqi;
import Model.Cliente;
import Model.PagoCulqi;
import Model.PlanJumping;
import Util.ConexionSqlSingleton;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoCulqiDaoImpl implements IPagoCulqi {

    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final PlanJumpingDaoImpl planDao = new PlanJumpingDaoImpl();

    @Override
    public boolean crear(PagoCulqi p) {
        String sql = "INSERT INTO pago_culqi_jx "
                + "(id_cliente, id_plan, metodo, monto, moneda, culqi_charge_id, culqi_order_id, estado, respuesta_json) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql, new String[]{"id_pago"})) {
            st.setInt(1, p.getCliente().getId_cliente());
            st.setInt(2, p.getPlan().getId_plan());
            st.setString(3, p.getMetodo());
            st.setDouble(4, p.getMonto());
            st.setString(5, p.getMoneda());
            st.setString(6, p.getCulqiChargeId());
            st.setString(7, p.getCulqiOrderId());
            st.setString(8, p.getEstado());
            st.setString(9, p.getRespuestaJson());
            int r = st.executeUpdate();
            if (r == 0) return false;
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) p.setIdPago(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al crear pago Culqi <<<");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizarPorCargo(String culqiChargeId, String estado, String respuestaJson) {
        String sql = "UPDATE pago_culqi_jx SET estado = ?, respuesta_json = ?, fecha_confirmacion = SYSTIMESTAMP "
                + "WHERE culqi_charge_id = ?";
        return actualizar(sql, estado, respuestaJson, culqiChargeId);
    }

    @Override
    public boolean actualizarPorOrden(String culqiOrderId, String estado, String respuestaJson) {
        String sql = "UPDATE pago_culqi_jx SET estado = ?, respuesta_json = ?, fecha_confirmacion = SYSTIMESTAMP "
                + "WHERE culqi_order_id = ?";
        return actualizar(sql, estado, respuestaJson, culqiOrderId);
    }

    private boolean actualizar(String sql, String estado, String respuestaJson, String idExterno) {
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            st.setString(1, estado);
            st.setString(2, respuestaJson);
            st.setString(3, idExterno);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al actualizar pago Culqi <<<");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marca el pago como PAGADO y crea el registrojumping que extiende la
     * vigencia del cliente. Todo en una transacción (igual que el flujo Yape
     * manual anterior): si algo falla, no queda a medias.
     */
    @Override
    public boolean marcarConfirmadoYExtenderPlan(int idPago) {
        PagoCulqi pago = SearchById(idPago);
        if (pago == null || "PAGADO".equals(pago.getEstado())) {
            return false; // no existe o ya se había procesado (evita duplicar el registro)
        }
        String sqlRegistro = "INSERT INTO registrojumping (id_cliente, id_plan, id_horario, metodo_pago, fecha_ingreso, monto) "
                + "VALUES (?, ?, NULL, ?, SYSDATE, ?)";
        String sqlPago = "UPDATE pago_culqi_jx SET estado = 'PAGADO', fecha_confirmacion = SYSTIMESTAMP, "
                + "id_registro_generado = ? WHERE id_pago = ?";

        try (Connection cn = ConexionSqlSingleton.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int idRegistro;
                try (PreparedStatement st = cn.prepareStatement(sqlRegistro, new String[]{"id_registro"})) {
                    st.setInt(1, pago.getCliente().getId_cliente());
                    st.setInt(2, pago.getPlan().getId_plan());
                    st.setString(3, "YAPE".equals(pago.getMetodo()) ? "YAPE" : "TARJETA");
                    st.setDouble(4, pago.getMonto());
                    st.executeUpdate();
                    try (ResultSet rs = st.getGeneratedKeys()) {
                        rs.next();
                        idRegistro = rs.getInt(1);
                    }
                }
                try (PreparedStatement up = cn.prepareStatement(sqlPago)) {
                    up.setInt(1, idRegistro);
                    up.setInt(2, idPago);
                    up.executeUpdate();
                }
                cn.commit();
                return true;
            } catch (SQLException e) {
                cn.rollback();
                System.err.println(">>> ERROR SQL al confirmar pago Culqi (se revirtió todo) <<<");
                e.printStackTrace();
                return false;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL de conexión al confirmar pago Culqi <<<");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PagoCulqi SearchById(int idPago) {
        return buscarUno("SELECT * FROM pago_culqi_jx WHERE id_pago = ?", idPago, null);
    }

    @Override
    public PagoCulqi SearchByOrderId(String culqiOrderId) {
        return buscarUno("SELECT * FROM pago_culqi_jx WHERE culqi_order_id = ?", -1, culqiOrderId);
    }

    private PagoCulqi buscarUno(String sql, int idNum, String idTxt) {
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            if (idTxt != null) st.setString(1, idTxt); else st.setInt(1, idNum);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al buscar pago Culqi <<<");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<PagoCulqi> listaPorCliente(int idCliente) {
        return listar("SELECT * FROM pago_culqi_jx WHERE id_cliente = " + idCliente + " ORDER BY fecha_creacion DESC");
    }

    @Override
    public List<PagoCulqi> listaTodos() {
        return listar("SELECT * FROM pago_culqi_jx ORDER BY fecha_creacion DESC");
    }

    private List<PagoCulqi> listar(String sql) {
        List<PagoCulqi> lista = new ArrayList<>();
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al listar pagos Culqi <<<");
            e.printStackTrace();
        }
        return lista;
    }

    private PagoCulqi mapear(ResultSet rs) throws SQLException {
        PagoCulqi p = new PagoCulqi();
        p.setIdPago(rs.getInt("id_pago"));
        Cliente c = clienteDao.SearchById(rs.getInt("id_cliente"));
        p.setCliente(c);
        PlanJumping plan = planDao.SearchById(rs.getInt("id_plan"));
        p.setPlan(plan);
        p.setMetodo(rs.getString("metodo"));
        p.setMonto(rs.getDouble("monto"));
        p.setMoneda(rs.getString("moneda"));
        p.setCulqiChargeId(rs.getString("culqi_charge_id"));
        p.setCulqiOrderId(rs.getString("culqi_order_id"));
        p.setEstado(rs.getString("estado"));
        p.setRespuestaJson(rs.getString("respuesta_json"));
        p.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        p.setFechaConfirmacion(rs.getTimestamp("fecha_confirmacion"));
        int idRegistro = rs.getInt("id_registro_generado");
        p.setIdRegistroGenerado(rs.wasNull() ? null : idRegistro);
        return p;
    }
}
