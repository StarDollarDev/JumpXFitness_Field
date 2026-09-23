
package Dao;

import Interface.IPlanJumping;
import Model.PlanJumping;
import Util.ConexionSqlSingleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanJumpingDaoImpl implements IPlanJumping{
    
    private Connection cn;

    @Override
    public List<PlanJumping> lista() {
        List<PlanJumping> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM planjumping ORDER BY id_plan";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                PlanJumping p = new PlanJumping();
                p.setId_plan(rs.getInt("id_plan"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecio(rs.getDouble("precio"));
                p.setCantidadPersonas(rs.getInt("cantidad_personas"));
                p.setDiasVigencia(rs.getInt("dias_vigencia"));
                p.setActivo((rs.getInt("activo") == 1));
                p.setImagen(rs.getString("imagen"));
                lista.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error al listar planes: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(PlanJumping plan) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO planjumping (nombre, precio, cantidad_personas, dias_vigencia, activo, imagen) VALUES (?, ?, ?, ?, ?, ?)";
            st = cn.prepareStatement(query, new String[]{"id_plan"});
            st.setString(1, plan.getNombre());
            st.setDouble(2, plan.getPrecio());
            st.setInt(3, plan.getCantidadPersonas());
            st.setInt(4, plan.getDiasVigencia());
            st.setInt(5, plan.isActivo() ? 1 : 0);
            st.setString(6, plan.getImagen());
            
            int r = st.executeUpdate();
            resultado = r > 0;
            
            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    plan.setId_plan(rs.getInt(1));
                }
                System.out.println("Plan registrado correctamente con ID: " + plan.getId_plan());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar plan: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public boolean update(PlanJumping plan) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "UPDATE planjumping SET nombre=?, precio=?, cantidad_personas=?, dias_vigencia=?, activo=?, imagen=? WHERE id_plan=?";
            st = cn.prepareStatement(query);
            st.setString(1, plan.getNombre());
            st.setDouble(2, plan.getPrecio());
            st.setInt(3, plan.getCantidadPersonas());
            st.setInt(4, plan.getDiasVigencia());
            st.setInt(5, plan.isActivo() ? 1 : 0);
            st.setString(6, plan.getImagen());
            st.setInt(7, plan.getId_plan());
            
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar plan: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public PlanJumping SearchById(int id) {
        PlanJumping plan = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM planjumping WHERE id_plan = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                plan = new PlanJumping();
                plan.setId_plan(rs.getInt("id_plan"));
                plan.setNombre(rs.getString("nombre"));
                plan.setPrecio(rs.getDouble("precio"));
                plan.setCantidadPersonas(rs.getInt("cantidad_personas"));
                plan.setDiasVigencia(rs.getInt("dias_vigencia"));
                plan.setActivo((rs.getInt("activo") == 1));
                plan.setImagen(rs.getString("imagen"));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar plan: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return plan;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "DELETE FROM planjumping WHERE id_plan = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar plan: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }
    
    @Override
    public List<PlanJumping> listaActivos() {
        List<PlanJumping> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM planjumping WHERE activo = 1 ORDER BY precio ASC";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                PlanJumping p = new PlanJumping();
                p.setId_plan(rs.getInt("id_plan"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecio(rs.getDouble("precio"));
                p.setCantidadPersonas(rs.getInt("cantidad_personas"));
                p.setDiasVigencia(rs.getInt("dias_vigencia"));
                p.setActivo((rs.getInt("activo") == 1));
                p.setImagen(rs.getString("imagen"));
                lista.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error al listar planes activos: " + e.getMessage());
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
