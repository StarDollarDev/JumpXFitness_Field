package Dao;

import Interface.ICliente;
import Model.Cliente;
import Model.Persona;
import Util.ConexionSqlSingleton;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;

public class ClienteDaoImpl implements ICliente {

    private Connection cn;
    private PersonaDaoImpl personaDAO;

    public ClienteDaoImpl() {
        this.personaDAO = new PersonaDaoImpl();
    }

    @Override
    public List<Cliente> lista() {
        List<Cliente> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM cliente ORDER BY id_cliente";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId_cliente(rs.getInt("id_cliente"));
                Persona p = personaDAO.SearchById(rs.getInt("id_persona"));
                c.setPersona(p);
                lista.add(c);
            }
        } catch (Exception e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(Cliente cliente) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            boolean personaInsert = personaDAO.insertSoloPersona(cliente.getPersona(), null);
            if (!personaInsert) {
                return false;
            }
            String query = "INSERT INTO cliente (id_persona) VALUES (?)";
            st = cn.prepareStatement(query, new String[]{"id_cliente"});
            st.setInt(1, cliente.getPersona().getId_persona());

            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    cliente.setId_cliente(rs.getInt(1));
                }
                System.out.println("Cliente registrado correctamente con ID: " + cliente.getId_cliente());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public Cliente crearParaPersonaExistente(int idPersona) {
        Cliente cliente = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "INSERT INTO cliente (id_persona) VALUES (?)";
            st = cn.prepareStatement(query, new String[]{"id_cliente"});
            st.setInt(1, idPersona);
            int r = st.executeUpdate();
            if (r > 0) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    cliente = new Cliente();
                    cliente.setId_cliente(rs.getInt(1));
                    cliente.setPersona(personaDAO.SearchById(idPersona));
                }
            }
        } catch (Exception e) {
            System.out.println("Error al crear cliente para persona existente: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return cliente;
    }

    @Override
    public boolean update(Cliente cliente) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            boolean personaUpdate = personaDAO.update(cliente.getPersona());
            if (!personaUpdate) {
                return false;
            }
            String query = "UPDATE cliente SET id_persona=? WHERE id_cliente=?";
            st = cn.prepareStatement(query);
            st.setInt(1, cliente.getPersona().getId_persona());
            st.setInt(2, cliente.getId_cliente());

            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public Cliente SearchById(int id) {
        Cliente cliente = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM cliente WHERE id_cliente = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                cliente = new Cliente();
                cliente.setId_cliente(rs.getInt("id_cliente"));
                Persona p = personaDAO.SearchById(rs.getInt("id_persona"));
                cliente.setPersona(p);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return cliente;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            Cliente cliente = SearchById(id);
            if (cliente == null) {
                return false;
            }
            String query = "DELETE FROM cliente WHERE id_cliente = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                personaDAO.delete(cliente.getPersona().getId_persona());
            }
        } catch (Exception e) {
            System.out.println("Error al eliminar cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public Cliente SearchByPersonaId(int idPersona) {
        Cliente cliente = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM cliente WHERE id_persona = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, idPersona);
            rs = st.executeQuery();
            if (rs.next()) {
                cliente = new Cliente();
                cliente.setId_cliente(rs.getInt("id_cliente"));
                Persona p = personaDAO.SearchById(rs.getInt("id_persona"));
                cliente.setPersona(p);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar cliente por persona: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return cliente;
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
