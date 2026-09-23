
package Dao;

import Interface.IHorario;
import Model.Horario;
import Util.ConexionSqlSingleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HorarioDaoImpl implements IHorario{
    private Connection cn;

    @Override
    public List<Horario> lista() {
        List<Horario> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            cn = ConexionSqlSingleton.getConnection();

            String sql = "SELECT * FROM horario ORDER BY hora_inicio";

            st = cn.prepareStatement(sql);
            rs = st.executeQuery();

            while (rs.next()) {

                Horario h = new Horario();

                h.setId_horario(rs.getInt("id_horario"));
                h.setHora_inicio(rs.getString("hora_inicio"));
                h.setHora_fin(rs.getString("hora_fin"));
                h.setActivo((rs.getInt("activo") == 1));

                lista.add(h);
            }

        } catch (Exception e) {
            System.out.println("Error al listar horarios: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }

        return lista;
    }

    @Override
    public boolean insertar(Horario horario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;

        try {

            cn = ConexionSqlSingleton.getConnection();

            String sql = "INSERT INTO horario(hora_inicio, hora_fin, activo) VALUES(?,?,?)";

            st = cn.prepareStatement(sql, new String[]{"id_horario"});

            st.setString(1, horario.getHora_inicio());
            st.setString(2, horario.getHora_fin());
            st.setInt(3, horario.isActivo() ? 1 : 0);

            resultado = st.executeUpdate() > 0;

            if (resultado) {

                rs = st.getGeneratedKeys();

                if (rs.next()) {
                    horario.setId_horario(rs.getInt(1));
                }

            }

        } catch (Exception e) {
            System.out.println("Error al insertar horario: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }

        return resultado;
    }

    @Override
    public boolean update(Horario horario) {
        PreparedStatement st = null;
        boolean resultado = false;

        try {

            cn = ConexionSqlSingleton.getConnection();

            String sql = "UPDATE horario SET hora_inicio=?, hora_fin=?, activo=? WHERE id_horario=?";

            st = cn.prepareStatement(sql);

            st.setString(1, horario.getHora_inicio());
            st.setString(2, horario.getHora_fin());
            st.setInt(3, horario.isActivo() ? 1 : 0);
            st.setInt(4, horario.getId_horario());

            resultado = st.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error al actualizar horario: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }

        return resultado;
    }

    @Override
    public Horario SearchById(int id) {
        Horario horario = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {

            cn = ConexionSqlSingleton.getConnection();
            String sql = "SELECT * FROM horario WHERE id_horario=?";
            st = cn.prepareStatement(sql);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {

                horario = new Horario();
                horario.setId_horario(rs.getInt("id_horario"));
                horario.setHora_inicio(rs.getString("hora_inicio"));
                horario.setHora_fin(rs.getString("hora_fin"));
                horario.setActivo((rs.getInt("activo") == 1));

            }

        } catch (Exception e) {
            System.out.println("Error al buscar horario: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return horario;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;

        try {
            cn = ConexionSqlSingleton.getConnection();
            String sql = "DELETE FROM horario WHERE id_horario=?";
            st = cn.prepareStatement(sql);
            st.setInt(1, id);
            resultado = st.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error al eliminar horario: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }
    
    @Override
    public List<Horario> listaActivos() {
        List<Horario> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM horario WHERE activo = 1 ORDER BY hora_inicio";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                Horario h = new Horario();
                h.setId_horario(rs.getInt("id_horario"));
                h.setHora_inicio(rs.getString("hora_inicio"));
                h.setHora_fin(rs.getString("hora_fin"));
                h.setActivo((rs.getInt("activo") == 1));
                lista.add(h);
            }
        } catch (Exception e) {
            System.out.println("Error al listar horarios activos: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }
    
    private void cerrarRecursos(ResultSet rs, PreparedStatement st) {
        try {
            if (rs != null) rs.close();
            if (st != null) st.close();
        } catch (Exception ex) {
            System.out.println("Error cerrando recursos: " + ex.getMessage());
        }
    }

    
}
