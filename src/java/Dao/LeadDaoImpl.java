package Dao;

import Interface.ILead;
import Model.Lead;
import Util.ConexionSqlSingleton;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeadDaoImpl implements ILead {

    @Override
    public boolean insertar(Lead lead) {
        String sql = "INSERT INTO lead_jx " +
                "(nombre_completo, correo, telefono, id_plan, plan_nombre, estado) " +
                "VALUES (?, ?, ?, ?, ?, 'NUEVO')";

        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql, new String[]{"id_lead"})) {

            st.setString(1, lead.getNombreCompleto());
            st.setString(2, lead.getCorreo());
            st.setString(3, lead.getTelefono());
            if (lead.getIdPlan() == null) {
                st.setNull(4, Types.INTEGER);
            } else {
                st.setInt(4, lead.getIdPlan());
            }
            st.setString(5, lead.getPlanNombre());

            int rows = st.executeUpdate();
            if (rows == 0) {
                return false;
            }

            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs != null && rs.next()) {
                    lead.setIdLead(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al insertar lead <<<");
            System.err.println("Mensaje : " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Código  : " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Lead> lista() {
        List<Lead> lista = new ArrayList<>();
        String sql = "SELECT id_lead, nombre_completo, correo, telefono, id_plan, " +
                "plan_nombre, estado, fecha_registro " +
                "FROM lead_jx ORDER BY fecha_registro DESC";

        try (Connection cn = ConexionSqlSingleton.getConnection();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Lead l = new Lead();
                l.setIdLead(rs.getInt("id_lead"));
                l.setNombreCompleto(rs.getString("nombre_completo"));
                l.setCorreo(rs.getString("correo"));
                l.setTelefono(rs.getString("telefono"));

                int idPlan = rs.getInt("id_plan");
                l.setIdPlan(rs.wasNull() ? null : idPlan);

                l.setPlanNombre(rs.getString("plan_nombre"));
                l.setEstado(rs.getString("estado"));
                l.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                lista.add(l);
            }

        } catch (SQLException e) {
            System.err.println(">>> ERROR SQL al listar leads <<<");
            System.err.println("Mensaje : " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Código  : " + e.getErrorCode());
            e.printStackTrace();
        }
        return lista;
    }
}