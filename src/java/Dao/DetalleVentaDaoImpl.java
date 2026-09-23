package Dao;

import Interface.IDetalleVenta;
import Model.DetalleVenta;
import Model.Producto;
import Model.Venta;
import Util.ConexionSqlSingleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDaoImpl implements IDetalleVenta {

    private Connection cn;
    private ProductoDaoImpl productoDAO;
    private VentaDaoImpl ventaDAO;

    public DetalleVentaDaoImpl() {
        this.productoDAO = new ProductoDaoImpl();
        this.ventaDAO = new VentaDaoImpl();
    }

    @Override
    public List<DetalleVenta> lista() {
        List<DetalleVenta> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM detalleventa ORDER BY id_detalle";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                DetalleVenta d = new DetalleVenta();
                d.setId_detalle(rs.getInt("id_detalle"));
                Venta v = ventaDAO.SearchById(rs.getInt("id_venta"));
                d.setVenta(v);
                Producto p = productoDAO.SearchById(rs.getInt("id_producto"));
                d.setProducto(p);
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecio(rs.getDouble("precio"));
                d.setSubtotal(rs.getDouble("subtotal"));
                lista.add(d);
            }
        } catch (Exception e) {
            System.out.println("Error al listar detalles de venta: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(DetalleVenta detalle) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO detalleventa"
                    + "(id_venta, id_producto, cantidad, precio, subtotal)"
                    + "VALUES (?,?,?,?,?)";
            st = cn.prepareStatement(query, new String[]{"id_detalle"});
            st.setInt(1, detalle.getVenta().getId_venta());
            st.setInt(2, detalle.getProducto().getId_producto());
            st.setInt(3, detalle.getCantidad());
            st.setDouble(4, detalle.getPrecio());
            st.setDouble(5, detalle.getSubtotal());

            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    detalle.setId_detalle(rs.getInt(1));
                }
                System.out.println("Detalle de venta registrado correctamente con ID: " + detalle.getId_detalle());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar detalle de venta: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public boolean update(DetalleVenta detalle) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "UPDATE detalleventa SET id_venta=?, id_producto=?, cantidad=?, precio=?, subtotal=?"
                    + "WHERE id_detalle=?";
            st = cn.prepareStatement(query);
            st.setInt(1, detalle.getVenta().getId_venta());
            st.setInt(2, detalle.getProducto().getId_producto());
            st.setInt(3, detalle.getCantidad());
            st.setDouble(4, detalle.getPrecio());
            st.setDouble(5, detalle.getSubtotal());
            st.setInt(6, detalle.getId_detalle());

            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar detalle de venta: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public DetalleVenta SearchById(int id) {
        DetalleVenta detalle = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM detalleventa WHERE id_detalle = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                detalle = new DetalleVenta();
                detalle.setId_detalle(rs.getInt("id_detalle"));
                Venta v = ventaDAO.SearchById(rs.getInt("id_venta"));
                detalle.setVenta(v);
                Producto p = productoDAO.SearchById(rs.getInt("id_producto"));
                detalle.setProducto(p);
                detalle.setCantidad(rs.getInt("cantidad"));
                detalle.setPrecio(rs.getDouble("precio"));
                detalle.setSubtotal(rs.getDouble("subtotal"));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar detalle de venta: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return detalle;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "DELETE FROM detalleventa WHERE id_detalle = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar detalle de venta: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
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

    @Override
    public List<DetalleVenta> SearchByVentaId(int idVenta) {
        List<DetalleVenta> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM detalleventa WHERE id_venta = ? ORDER BY id_detalle";
            st = cn.prepareStatement(query);
            st.setInt(1, idVenta);
            rs = st.executeQuery();
            while (rs.next()) {
                DetalleVenta d = new DetalleVenta();
                d.setId_detalle(rs.getInt("id_detalle"));

                Venta v = ventaDAO.SearchById(rs.getInt("id_venta"));
                d.setVenta(v);

                Producto p = productoDAO.SearchById(rs.getInt("id_producto"));
                d.setProducto(p);

                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecio(rs.getDouble("precio"));
                d.setSubtotal(rs.getDouble("subtotal"));
                lista.add(d);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar detalles por venta: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<DetalleVenta> SearchByClienteId(int idCliente) {
        List<DetalleVenta> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT dv.* FROM detalleventa dv "
                    + "INNER JOIN venta v ON dv.id_venta = v.id_venta "
                    + "WHERE v.id_cliente = ? ORDER BY dv.id_detalle";
            st = cn.prepareStatement(query);
            st.setInt(1, idCliente);
            rs = st.executeQuery();
            while (rs.next()) {
                DetalleVenta d = new DetalleVenta();
                d.setId_detalle(rs.getInt("id_detalle"));

                Venta v = ventaDAO.SearchById(rs.getInt("id_venta"));
                d.setVenta(v);

                Producto p = productoDAO.SearchById(rs.getInt("id_producto"));
                d.setProducto(p);

                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecio(rs.getDouble("precio"));
                d.setSubtotal(rs.getDouble("subtotal"));
                lista.add(d);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar detalles por cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }
}
