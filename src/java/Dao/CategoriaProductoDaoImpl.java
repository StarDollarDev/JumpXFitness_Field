
package Dao;

import Interface.ICategoriaProducto;
import Model.CategoriaProducto;
import Util.ConexionSqlSingleton;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;

public class CategoriaProductoDaoImpl implements ICategoriaProducto{
    private Connection cn;

    @Override
    public List<CategoriaProducto> lista() {
        List<CategoriaProducto> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM categoria ORDER BY id_categoria";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                CategoriaProducto c = new CategoriaProducto();
                c.setId_categoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                lista.add(c);
            }
        } catch (Exception e) {
            System.out.println("Error al listar categorias: " +e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(CategoriaProducto categoria) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO categoria (nombre) VALUES (?)";
            st = cn.prepareStatement(query, new String[]{"id_categoria"});
            st.setString(1, categoria.getNombre());
            int r = st.executeUpdate();
            resultado = r>0;
            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    categoria.setId_categoria(rs.getInt(1));
                }
                System.out.println("Categoria Registrada correctamente con ID: "+ categoria.getId_categoria());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar categoria: " +e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public boolean update(CategoriaProducto categoria) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "UPDATE categoria SET nombre=? WHERE id_categoria=?";
            st = cn.prepareStatement(query);
            st.setString(1, categoria.getNombre());
            st.setInt(2, categoria.getId_categoria());
            
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar categoria: " +e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public CategoriaProducto SearchById(int id) {
        CategoriaProducto categoria = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM categoria WHERE id_categoria = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                categoria = new CategoriaProducto();
                categoria.setId_categoria(rs.getInt("id_categoria"));
                categoria.setNombre(rs.getString("nombre"));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar categoria: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return categoria;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "DELETE FROM categoria WHERE id_categoria = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar categoría: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
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
