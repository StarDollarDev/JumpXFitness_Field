package Dao;

import Interface.IRegistroJumping;
import Model.Cliente;
import Model.Horario;
import Model.MetodoPago;
import Model.PlanJumping;
import Model.RegistroJumping;
import Util.ConexionSqlSingleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

public class RegistroJumpingDaoImpl implements IRegistroJumping {

    private Connection cn;
    private ClienteDaoImpl clienteDAO;
    private PlanJumpingDaoImpl planDAO;
    private HorarioDaoImpl horarioDAO;

    public RegistroJumpingDaoImpl() {
        this.clienteDAO = new ClienteDaoImpl();
        this.planDAO = new PlanJumpingDaoImpl();
        this.horarioDAO = new HorarioDaoImpl();
    }

    @Override
    public List<RegistroJumping> lista() {
        List<RegistroJumping> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM registrojumping ORDER BY id_registro";
            st = cn.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                RegistroJumping r = new RegistroJumping();
                r.setId_registro(rs.getInt("id_registro"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                PlanJumping p = planDAO.SearchById(rs.getInt("id_plan"));
                r.setPlan(p);
                Horario h = horarioDAO.SearchById(rs.getInt("id_horario"));
                r.setHorario(h);
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFechaIngreso(rs.getDate("fecha_ingreso"));
                r.setMonto(rs.getDouble("monto"));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al listar registros jumping: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public boolean insertar(RegistroJumping registro) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();

            String query = "INSERT INTO registrojumping (id_cliente, id_plan, id_horario, metodo_pago, fecha_ingreso, monto) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            st = cn.prepareStatement(query, new String[]{"id_registro"});
            st.setInt(1, registro.getCliente().getId_cliente());
            st.setInt(2, registro.getPlan().getId_plan());
            st.setInt(3, registro.getHorario().getId_horario());
            st.setString(4, registro.getMetodoPago().name());
            st.setDate(5, registro.getFechaIngreso());
            st.setDouble(6, registro.getMonto());

            int r = st.executeUpdate();
            resultado = r > 0;

            if (resultado) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    registro.setId_registro(rs.getInt(1));
                }
                System.out.println("Registro jumping registrado correctamente con ID: " + registro.getId_registro());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar registro jumping: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return resultado;
    }

    @Override
    public boolean update(RegistroJumping registro) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();

            String query = "UPDATE registrojumping SET id_cliente=?, id_plan=?, id_horario=?, metodo_pago=?, fecha_ingreso=?, monto=? "
                    + "WHERE id_registro=?";
            st = cn.prepareStatement(query);
            st.setInt(1, registro.getCliente().getId_cliente());
            st.setInt(2, registro.getPlan().getId_plan());
            st.setInt(3, registro.getHorario().getId_horario());
            st.setString(4, registro.getMetodoPago().name());
            st.setDate(5, registro.getFechaIngreso());
            st.setDouble(6, registro.getMonto());
            st.setInt(7, registro.getId_registro());

            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar registro jumping: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public RegistroJumping SearchById(int id) {
        RegistroJumping registro = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM registrojumping WHERE id_registro = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                registro = new RegistroJumping();
                registro.setId_registro(rs.getInt("id_registro"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                registro.setCliente(c);
                PlanJumping p = planDAO.SearchById(rs.getInt("id_plan"));
                registro.setPlan(p);
                Horario h = horarioDAO.SearchById(rs.getInt("id_horario"));
                registro.setHorario(h);
                registro.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                registro.setFechaIngreso(rs.getDate("fecha_ingreso"));
                registro.setMonto(rs.getDouble("monto"));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar registro jumping: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return registro;
    }

    @Override
    public boolean delete(int id) {
        PreparedStatement st = null;
        boolean resultado = false;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "DELETE FROM registrojumping WHERE id_registro = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, id);
            int r = st.executeUpdate();
            resultado = r > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar registro jumping: " + e.getMessage());
        } finally {
            cerrarRecursos(null, st);
        }
        return resultado;
    }

    @Override
    public List<RegistroJumping> getRegistrosByFecha(java.sql.Date fecha) {
        List<RegistroJumping> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM registrojumping WHERE fecha_ingreso = ? ORDER BY id_horario";
            st = cn.prepareStatement(query);
            st.setDate(1, (Date) fecha);
            rs = st.executeQuery();
            while (rs.next()) {
                RegistroJumping r = new RegistroJumping();
                r.setId_registro(rs.getInt("id_registro"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                PlanJumping p = planDAO.SearchById(rs.getInt("id_plan"));
                r.setPlan(p);
                Horario h = horarioDAO.SearchById(rs.getInt("id_horario"));
                r.setHorario(h);
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFechaIngreso(rs.getDate("fecha_ingreso"));
                r.setMonto(rs.getDouble("monto"));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al obtener registros por fecha: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    @Override
    public List<Horario> getHorariosDisponibles(java.sql.Date fecha) {
        List<Horario> horariosDisponibles = new ArrayList<>();
        List<Horario> todosLosHorarios = horarioDAO.lista();
        List<Integer> horariosOcupados = getHorariosOcupadosIds(fecha);

        for (Horario h : todosLosHorarios) {
            if (!horariosOcupados.contains(h.getId_horario())) {
                horariosDisponibles.add(h);
            }
        }
        return horariosDisponibles;
    }

    @Override
    public boolean isHorarioOcupado(int idHorario, java.sql.Date fecha) {
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean ocupado = false;

        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT COUNT(*) FROM registrojumping WHERE id_horario = ? AND fecha_ingreso = ?";
            st = cn.prepareStatement(query);
            st.setInt(1, idHorario);
            st.setDate(2, fecha);
            rs = st.executeQuery();
            if (rs.next()) {
                ocupado = rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println("Error al verificar disponibilidad de horario: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return ocupado;
    }

    @Override
    public List<Horario> getHorariosOcupados(java.sql.Date fecha) {
        List<Horario> horariosOcupados = new ArrayList<>();
        List<Integer> idsOcupados = getHorariosOcupadosIds(fecha);

        for (Integer id : idsOcupados) {
            Horario h = horarioDAO.SearchById(id);
            if (h != null) {
                horariosOcupados.add(h);
            }
        }
        return horariosOcupados;
    }

    @Override
    public List<RegistroJumping> SearchByClienteId(int idCliente) {
        List<RegistroJumping> lista = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT * FROM registrojumping WHERE id_cliente = ? ORDER BY fecha_ingreso DESC, id_horario";
            st = cn.prepareStatement(query);
            st.setInt(1, idCliente);
            rs = st.executeQuery();
            while (rs.next()) {
                RegistroJumping r = new RegistroJumping();
                r.setId_registro(rs.getInt("id_registro"));
                Cliente c = clienteDAO.SearchById(rs.getInt("id_cliente"));
                r.setCliente(c);
                PlanJumping p = planDAO.SearchById(rs.getInt("id_plan"));
                r.setPlan(p);
                Horario h = horarioDAO.SearchById(rs.getInt("id_horario"));
                r.setHorario(h);
                r.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                r.setFechaIngreso(rs.getDate("fecha_ingreso"));
                r.setMonto(rs.getDouble("monto"));
                lista.add(r);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar registros por cliente: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return lista;
    }

    private List<Integer> getHorariosOcupadosIds(Date fecha) {
        List<Integer> horariosOcupados = new ArrayList<>();
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            cn = ConexionSqlSingleton.getConnection();
            String query = "SELECT DISTINCT id_horario FROM registrojumping WHERE fecha_ingreso = ?";
            st = cn.prepareStatement(query);
            st.setDate(1, fecha);
            rs = st.executeQuery();
            while (rs.next()) {
                horariosOcupados.add(rs.getInt("id_horario"));
            }
        } catch (Exception e) {
            System.out.println("Error al obtener horarios ocupados: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
        }
        return horariosOcupados;
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
