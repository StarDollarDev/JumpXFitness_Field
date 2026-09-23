package Dao;

import Interface.IVenta;
import Model.Cliente;
import Model.MetodoPago;
import java.sql.*;
import Model.Venta;
import Util.ConexionSqlSingleton;
import java.util.ArrayList;
import java.util.List;

public class VentaDaoImpl implements IVenta {

    private Connection cn;
    private ClienteDaoImpl clienteDAO;

    public VentaDaoImpl() {
        this.clienteDAO = new ClienteDaoImpl();
    }

    @Override
    public List<Venta> lista() {
        List<Venta> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM venta ORDER BY id_venta";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                Venta v = new Venta();
                v.setId_venta(rs.getInt("id_venta"));

                int idCliente = rs.getInt("id_cliente");
                if (!rs.wasNull()) {
                    Cliente c = clienteDAO.SearchById(idCliente);
                    v.setCliente(c);
                }

                v.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                v.setFecha(rs.getDate("fecha"));
                v.setTotal(rs.getDouble("total"));
                lista.add(v);
            }
        } catch (Exception e) {
            System.out.println("Error al listar ventas: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(Venta venta) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO venta (id_cliente, metodo_pago, fecha, total) "
                    + "VALUES (?, ?, ?, ?)";
            st = cn.prepareStatement(query, new String[]{"id_venta"});

            if (venta.getCliente() != null) {
                st.setInt(1, venta.getCliente().getId_cliente());
            } else {
                st.setNull(1, Types.INTEGER);
            }
            st.setString(2, venta.getMetodoPago().name());
            st.setDate(3, new Date(venta.getFecha().getTime()));
            st.setDouble(4, venta.getTotal());

            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    venta.setId_venta(rs.getInt(1));
                }
                System.out.println("Venta registrada correctamente con ID: " + venta.getId_venta());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar venta: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public boolean update(Venta venta) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "UPDATE venta SET id_cliente=?, metodo_pago=?, fecha=?, total=? WHERE id_venta=?";
            st = cn.prepareStatement(query);

            if (venta.getCliente() != null) {
                st.setInt(1, venta.getCliente().getId_cliente());
            } else {
                st.setNull(1, Types.INTEGER);
            }
            st.setString(2, venta.getMetodoPago().name());
            st.setDate(3, new Date(venta.getFecha().getTime()));
            st.setDouble(4, venta.getTotal());
            st.setInt(5, venta.getId_venta());

            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar venta: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public Venta SearchById(int id) {
        Venta venta = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM venta WHERE id_venta = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                venta = new Venta();
                venta.setId_venta(rs.getInt("id_venta"));

                int idCliente = rs.getInt("id_cliente");
                if (!rs.wasNull()) {
                    Cliente c = clienteDAO.SearchById(idCliente);
                    venta.setCliente(c);
                }

                venta.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                venta.setFecha(rs.getDate("fecha"));
                venta.setTotal(rs.getDouble("total"));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar venta: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return venta;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "DELETE FROM venta WHERE id_venta = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar venta: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public List<Venta> SearchByDateRange(java.util.Date inicio, java.util.Date fin) {
        List<Venta> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM Venta WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";
            st = cn.prepareStatement(query);
            st.setDate(1, new Date(inicio.getTime()));
            st.setDate(2, new Date(fin.getTime()));
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToVenta(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar ventas por rango de fechas: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<Venta> SearchByFecha(java.util.Date fecha) {
        List<Venta> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM venta WHERE TRUNC(fecha) = TRUNC(?)";
            st = cn.prepareStatement(query);
            st.setDate(1, new Date(fecha.getTime()));
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToVenta(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar ventas por fecha: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<Venta> SearchByClienteId(int idCliente) {
        List<Venta> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM venta WHERE id_cliente = ? ORDER BY fecha DESC";
            st = cn.prepareStatement(query);
            st.setInt(1, idCliente);
            rs = st.executeQuery();
            while (rs.next()) {
                Venta v = new Venta();
                v.setId_venta(rs.getInt("id_venta"));

                int idCli = rs.getInt("id_cliente");
                if (!rs.wasNull()) {
                    Cliente c = clienteDAO.SearchById(idCli);
                    v.setCliente(c);
                }

                v.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                v.setFecha(rs.getDate("fecha"));
                v.setTotal(rs.getDouble("total"));
                lista.add(v);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar ventas por cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    private Venta mapResultSetToVenta(ResultSet rs) throws SQLException, Exception {
        Venta venta = new Venta();
        venta.setId_venta(rs.getInt("id_venta"));

        int idCliente = rs.getInt("id_cliente");
        if (!rs.wasNull()) {
            Cliente c = new ClienteDaoImpl().SearchById(idCliente);
            venta.setCliente(c);
        }

        venta.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
        venta.setFecha(rs.getDate("fecha"));
        venta.setTotal(rs.getDouble("total"));
        return venta;
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
