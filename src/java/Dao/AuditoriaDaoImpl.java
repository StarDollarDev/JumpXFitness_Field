package Dao;

import Interface.IAuditoria;
import Model.Auditoria;
import Util.ConexionSqlSingleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDaoImpl implements IAuditoria {

    private Connection cn;

    @Override
    public boolean registrar(Auditoria a) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO auditoria "
                    + "(id_usuario, usuario_nombre, accion, tabla_afectada, id_registro_afectado, detalle, ip_origen) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            st = cn.prepareStatement(query);
            if (a.getId_usuario() != null) {
                st.setInt(1, a.getId_usuario());
            } else {
                st.setNull(1, Types.INTEGER);
            }
            st.setString(2, a.getUsuarioNombre());
            st.setString(3, a.getAccion());
            st.setString(4, a.getTablaAfectada());
            st.setString(5, a.getIdRegistroAfectado());
            st.setString(6, a.getDetalle());
            st.setString(7, a.getIpOrigen());

            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            // La auditoría nunca debe tumbar la operación de negocio: solo se registra el error.
            System.out.println("Error al registrar auditoria: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public List<Auditoria> lista() {
        return listaPaginada(500);
    }

    @Override
    public List<Auditoria> listaPaginada(int limite) {
        List<Auditoria> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM auditoria ORDER BY fecha_hora DESC "
                    + "FETCH FIRST ? ROWS ONLY";
            st = cn.prepareStatement(query);
            st.setInt(1, limite);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar auditoria: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<Auditoria> listarPorTabla(String tabla) {
        List<Auditoria> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM auditoria WHERE tabla_afectada = ? ORDER BY fecha_hora DESC";
            st = cn.prepareStatement(query);
            st.setString(1, tabla);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar auditoria por tabla: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<Auditoria> listarPorUsuario(int idUsuario) {
        List<Auditoria> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM auditoria WHERE id_usuario = ? ORDER BY fecha_hora DESC";
            st = cn.prepareStatement(query);
            st.setInt(1, idUsuario);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar auditoria por usuario: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<Auditoria> listarPorAccion(String accion) {
        List<Auditoria> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM auditoria WHERE accion = ? ORDER BY fecha_hora DESC";
            st = cn.prepareStatement(query);
            st.setString(1, accion);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar auditoria por accion: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<Auditoria> listarPorRangoFecha(String fechaInicio, String fechaFin) {
        List<Auditoria> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM auditoria "
                    + "WHERE fecha_hora >= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') "
                    + "AND fecha_hora <= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') "
                    + "ORDER BY fecha_hora DESC";
            st = cn.prepareStatement(query);
            st.setString(1, fechaInicio + " 00:00:00");
            st.setString(2, fechaFin + " 23:59:59");
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar auditoria por rango de fecha: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    private Auditoria mapear(ResultSet rs) throws SQLException {
        Auditoria a = new Auditoria();
        a.setId_auditoria(rs.getInt("id_auditoria"));
        int idUsuario = rs.getInt("id_usuario");
        a.setId_usuario(rs.wasNull() ? null : idUsuario);
        a.setUsuarioNombre(rs.getString("usuario_nombre"));
        a.setAccion(rs.getString("accion"));
        a.setTablaAfectada(rs.getString("tabla_afectada"));
        a.setIdRegistroAfectado(rs.getString("id_registro_afectado"));
        a.setDetalle(rs.getString("detalle"));
        a.setIpOrigen(rs.getString("ip_origen"));
        a.setFechaHora(rs.getTimestamp("fecha_hora"));
        return a;
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
