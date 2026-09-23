package Dao;

import Interface.IReclamo;
import Model.Reclamo;
import Util.ConexionSqlSingleton;

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class ReclamoDaoImpl implements IReclamo {

    @Override
    public boolean insertar(Reclamo r) {
        String sqlInsert = "INSERT INTO reclamo_jx " +
                "(nombre_completo, tipo_doc, numero_doc, direccion, telefono, correo, apoderado, " +
                "tipo_bien, descripcion_bien, monto, tipo_reclamo, detalle, pedido, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REGISTRADO')";

        try (Connection cn = ConexionSqlSingleton.getConnection()) {
            try (PreparedStatement st = cn.prepareStatement(sqlInsert, new String[]{"id_reclamo"})) {
                st.setString(1, r.getNombreCompleto());
                st.setString(2, r.getTipoDoc());
                st.setString(3, r.getNumeroDoc());
                st.setString(4, r.getDireccion());
                st.setString(5, r.getTelefono());
                st.setString(6, r.getCorreo());
                st.setString(7, r.getApoderado());
                st.setString(8, r.getTipoBien());
                st.setString(9, r.getDescripcionBien());
                st.setBigDecimal(10, r.getMonto());
                st.setString(11, r.getTipoReclamo());
                st.setString(12, r.getDetalle());
                st.setString(13, r.getPedido());

                int rows = st.executeUpdate();
                if (rows == 0) {
                    return false;
                }
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs != null && rs.next()) {
                        r.setIdReclamo(rs.getInt(1));
                    }
                }
            }

            // Código legible y correlativo: LR-2026-000123 (se arma tras conocer el id autogenerado)
            String codigo = "LR-" + Year.now() + "-" + String.format("%06d", r.getIdReclamo());
            try (PreparedStatement up = cn.prepareStatement(
                    "UPDATE reclamo_jx SET codigo = ? WHERE id_reclamo = ?")) {
                up.setString(1, codigo);
                up.setInt(2, r.getIdReclamo());
                up.executeUpdate();
            }
            r.setCodigo(codigo);
            return true;

        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al insertar reclamo <<<");
            System.err.println("Mensaje : " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Código  : " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Reclamo> lista() {
        List<Reclamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM reclamo_jx ORDER BY fecha_registro DESC";

        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al listar reclamos <<<");
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Reclamo buscarPorCodigo(String codigo) {
        String sql = "SELECT * FROM reclamo_jx WHERE codigo = ?";
        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            st.setString(1, codigo);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al buscar reclamo por código <<<");
            e.printStackTrace();
        }
        return null;
    }

    private Reclamo mapear(ResultSet rs) throws SQLException {
        Reclamo r = new Reclamo();
        r.setIdReclamo(rs.getInt("id_reclamo"));
        r.setCodigo(rs.getString("codigo"));
        r.setNombreCompleto(rs.getString("nombre_completo"));
        r.setTipoDoc(rs.getString("tipo_doc"));
        r.setNumeroDoc(rs.getString("numero_doc"));
        r.setDireccion(rs.getString("direccion"));
        r.setTelefono(rs.getString("telefono"));
        r.setCorreo(rs.getString("correo"));
        r.setApoderado(rs.getString("apoderado"));
        r.setTipoBien(rs.getString("tipo_bien"));
        r.setDescripcionBien(rs.getString("descripcion_bien"));
        r.setMonto(rs.getBigDecimal("monto"));
        r.setTipoReclamo(rs.getString("tipo_reclamo"));
        r.setDetalle(rs.getString("detalle"));
        r.setPedido(rs.getString("pedido"));
        r.setEstado(rs.getString("estado"));
        r.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return r;
    }
}
