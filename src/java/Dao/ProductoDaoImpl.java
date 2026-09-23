package Dao;

import Interface.IProducto;
import Model.CategoriaProducto;
import Model.Producto;
import Util.ConexionSqlSingleton;
import java.sql.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductoDaoImpl implements IProducto {

    private Connection cn;
    private CategoriaProductoDaoImpl categoriaDAO;

    public ProductoDaoImpl() {
        this.categoriaDAO = new CategoriaProductoDaoImpl();
    }

    @Override
    public List<Producto> lista() {
        List<Producto> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM producto ORDER BY id_producto";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();

            while (rs.next()) {
                Producto p = new Producto();
                p.setId_producto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecioCompra(rs.getDouble("precio_compra"));
                p.setPrecioVenta(rs.getDouble("precio_venta"));
                p.setStock(rs.getInt("stock"));
                CategoriaProducto c = categoriaDAO.SearchById(rs.getInt("id_categoria"));
                p.setCategoriaProducto(c);
                lista.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(Producto producto) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO producto (nombre, descripcion, precio_compra, "
                    + "precio_venta, stock, id_categoria) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            st = cn.prepareStatement(query, new String[]{"id_producto"});
            st.setString(1, producto.getNombre());
            st.setString(2, producto.getDescripcion());
            st.setDouble(3, producto.getPrecioCompra());
            st.setDouble(4, producto.getPrecioVenta());
            st.setInt(5, producto.getStock());
            st.setInt(6, producto.getCategoriaProducto().getId_categoria());

            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    producto.setId_producto(rs.getInt(1));
                }
                System.out.println("Producto registrado correctamente con ID: " + producto.getId_producto());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar producto: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public boolean update(Producto producto) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "UPDATE producto SET nombre=?, descripcion=?, precio_compra=?, precio_venta=?, stock=?, id_categoria=? "
                    + "WHERE id_producto=?";
            st = cn.prepareStatement(query);
            st.setString(1, producto.getNombre());
            st.setString(2, producto.getDescripcion());
            st.setDouble(3, producto.getPrecioCompra());
            st.setDouble(4, producto.getPrecioVenta());
            st.setInt(5, producto.getStock());
            st.setInt(6, producto.getCategoriaProducto().getId_categoria());
            st.setInt(7, producto.getId_producto());

            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar producto: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public Producto SearchById(int id) {
        Producto producto = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM producto WHERE id_producto = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                producto = new Producto();
                producto.setId_producto(rs.getInt("id_producto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setPrecioCompra(rs.getDouble("precio_compra"));
                producto.setPrecioVenta(rs.getDouble("precio_venta"));
                producto.setStock(rs.getInt("stock"));
                CategoriaProducto c = categoriaDAO.SearchById(rs.getInt("id_categoria"));
                producto.setCategoriaProducto(c);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return producto;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "DELETE FROM producto WHERE id_producto = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar producto: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public boolean updateStock(int id, int stock) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "UPDATE producto SET stock = ? WHERE id_producto = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, stock);
            st.setInt(2, id);

            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                System.out.println("Stock actualizado correctamente. Producto ID: " + id + ", Nuevo stock: " + stock);
            } else {
                System.out.println("No se encontró el producto con ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("Error al actualizar stock: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public List<Producto> SearchByStockMinimo(int stockMinimo) {
        List<Producto> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM producto WHERE stock <= ? ORDER BY stock ASC";

            st = cn.prepareStatement(query);
            st.setInt(1, stockMinimo);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToProducto(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar productos con stock bajo: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }
    
    @Override
    public List<Producto> SearchWithStock() {
        List<Producto> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM producto WHERE stock > 0 ORDER BY nombre";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToProducto(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar productos con stock: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }
    
    @Override
    public List<Producto> SearchByCategoriaId(int idCategoria) {
        List<Producto> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM producto WHERE id_categoria = ? ORDER BY nombre";
            st = cn.prepareStatement(query);
            st.setInt(1, idCategoria);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToProducto(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar productos por categoría: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }
    
    @Override
    public Producto SearchByNombreExact(String nombre) {
        Producto producto = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM producto WHERE nombre = ?";
            st = cn.prepareStatement(query);
            st.setString(1, nombre);
            rs = st.executeQuery();
            if (rs.next()) {
                producto = mapResultSetToProducto(rs);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar producto por nombre: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return producto;
    }
   
    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId_producto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecioCompra(rs.getDouble("precio_compra"));
        producto.setPrecioVenta(rs.getDouble("precio_venta"));
        producto.setStock(rs.getInt("stock"));

        // Obtener la categoría
        int idCategoria = rs.getInt("id_categoria");
        if (!rs.wasNull()) {
            CategoriaProducto categoria = categoriaDAO.SearchById(idCategoria);
            producto.setCategoriaProducto(categoria);
        }
        return producto;
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
