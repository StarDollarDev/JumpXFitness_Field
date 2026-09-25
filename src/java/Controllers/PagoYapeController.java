package Controllers;

import Dao.ClienteDaoImpl;
import Dao.PagoYapeDaoImpl;
import Dao.PlanJumpingDaoImpl;
import Model.Cliente;
import Model.PagoYape;
import Model.PlanJumping;
import Model.Rol;
import Model.Usuario;
import Util.AuditoriaHelper;
import Util.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Pagos por Yape para renovar/comprar un plan.
 *
 * IMPORTANTE: no existe una API pública de Yape para verificar pagos en
 * tiempo real desde un negocio pequeño. Este flujo es manual: el cliente
 * escribe el código de operación de SU comprobante de Yape, queda en estado
 * PENDIENTE, y un administrador lo revisa contra su propia app de Yape antes
 * de confirmar. Solo al confirmar se extiende la vigencia del cliente.
 */
@WebServlet(name = "PagoYapeController", urlPatterns = {"/PagoYapeController"})
public class PagoYapeController extends HttpServlet {

    private final PagoYapeDaoImpl pagoDao = new PagoYapeDaoImpl();
    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final PlanJumpingDaoImpl planDao = new PlanJumpingDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        JsonObject out = new JsonObject();

        try (PrintWriter writer = response.getWriter()) {
            Usuario sesion = usuarioDeSesion(request);

            if ("misPagos".equals(action)) {
                if (sesion == null || sesion.getRol() != Rol.CLIENTE) {
                    noAutorizado(response, writer, out);
                    return;
                }
                Cliente cliente = clienteDao.SearchByPersonaId(sesion.getPersona().getId_persona());
                var lista = cliente == null ? java.util.List.<PagoYape>of() : pagoDao.listaPorCliente(cliente.getId_cliente());
                out.addProperty("success", true);
                out.add("data", gson.toJsonTree(lista));
                writer.print(out);

            } else if ("pendientes".equals(action)) {
                if (sesion == null || sesion.getRol() != Rol.ADMIN) {
                    noAutorizado(response, writer, out);
                    return;
                }
                out.addProperty("success", true);
                out.add("data", gson.toJsonTree(pagoDao.listaPendientes()));
                writer.print(out);

            } else {
                out.addProperty("success", false);
                out.addProperty("message", "Acción no válida");
                writer.print(out);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        JsonObject out = new JsonObject();

        try (PrintWriter writer = response.getWriter()) {
            Usuario sesion = usuarioDeSesion(request);

            if ("solicitar".equals(action)) {
                if (sesion == null || sesion.getRol() != Rol.CLIENTE) {
                    noAutorizado(response, writer, out);
                    return;
                }
                if (!RateLimiter.permitir("pago-yape:" + sesion.getId_usuario(), 6, 30 * 60 * 1000L)) {
                    response.setStatus(429);
                    out.addProperty("success", false);
                    out.addProperty("message", "Ya enviaste varias solicitudes. Espera a que se revisen antes de enviar otra.");
                    writer.print(out);
                    return;
                }

                Cliente cliente = clienteDao.SearchByPersonaId(sesion.getPersona().getId_persona());
                if (cliente == null) {
                    out.addProperty("success", false);
                    out.addProperty("message", "Tu cuenta no tiene un perfil de cliente asociado. Contacta al administrador.");
                    writer.print(out);
                    return;
                }

                int idPlan = parseIntSeguro(request.getParameter("idPlan"));
                String codigoOperacion = limpiar(request, "codigoOperacion");
                String celularPagador = limpiar(request, "celularPagador");

                if (idPlan <= 0) {
                    fallar(writer, out, "Selecciona un plan.");
                    return;
                }
                PlanJumping plan = planDao.SearchById(idPlan);
                if (plan == null || !plan.isActivo()) {
                    fallar(writer, out, "El plan seleccionado ya no está disponible.");
                    return;
                }
                if (codigoOperacion.length() < 4 || codigoOperacion.length() > 30) {
                    fallar(writer, out, "Ingresa el código de operación de tu Yape.");
                    return;
                }
                if (celularPagador.length() < 7 || celularPagador.length() > 20) {
                    fallar(writer, out, "Ingresa el número desde el que pagaste por Yape.");
                    return;
                }

                PagoYape pago = new PagoYape();
                pago.setCliente(cliente);
                pago.setPlan(plan);
                pago.setMonto(plan.getPrecio());
                pago.setCodigoOperacion(codigoOperacion);
                pago.setCelularPagador(celularPagador);

                boolean ok = pagoDao.solicitar(pago);
                if (!ok) {
                    fallar(writer, out, "No se pudo registrar tu pago. Inténtalo nuevamente.");
                    return;
                }

                out.addProperty("success", true);
                out.addProperty("message", "Recibimos tu pago. Un administrador lo confirmará en cuanto lo verifique en Yape.");
                writer.print(out);

            } else if ("confirmar".equals(action) || "rechazar".equals(action)) {
                if (sesion == null || sesion.getRol() != Rol.ADMIN) {
                    noAutorizado(response, writer, out);
                    return;
                }
                int idPago = parseIntSeguro(request.getParameter("idPago"));
                if (idPago <= 0) {
                    fallar(writer, out, "Pago inválido.");
                    return;
                }

                boolean ok;
                if ("confirmar".equals(action)) {
                    ok = pagoDao.confirmar(idPago);
                    if (ok) {
                        AuditoriaHelper.registrar(request, "UPDATE", "pago_yape_jx", idPago,
                                "Pago Yape #" + idPago + " confirmado por '" + sesion.getUsuario() + "'");
                    }
                } else {
                    String motivo = limpiar(request, "motivo");
                    ok = pagoDao.rechazar(idPago, motivo.isEmpty() ? "Sin motivo especificado" : motivo);
                    if (ok) {
                        AuditoriaHelper.registrar(request, "UPDATE", "pago_yape_jx", idPago,
                                "Pago Yape #" + idPago + " rechazado por '" + sesion.getUsuario() + "'");
                    }
                }

                out.addProperty("success", ok);
                out.addProperty("message", ok ? "Listo." : "No se pudo procesar (¿ya estaba resuelto?).");
                writer.print(out);

            } else {
                out.addProperty("success", false);
                out.addProperty("message", "Acción no válida");
                writer.print(out);
            }
        } catch (Exception e) {
            response.setStatus(500);
            System.err.println(">>> ERROR GENERAL en PagoYapeController <<<");
            e.printStackTrace();
            out.addProperty("success", false);
            out.addProperty("message", "Ocurrió un error al procesar la solicitud.");
            try {
                response.getWriter().print(gson.toJson(out));
            } catch (IOException ignored) {
            }
        }
    }

    private Usuario usuarioDeSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Usuario) session.getAttribute("usuario");
    }

    private void noAutorizado(HttpServletResponse response, PrintWriter writer, JsonObject out) {
        response.setStatus(403);
        out.addProperty("success", false);
        out.addProperty("message", "No tienes permiso para realizar esta acción.");
        writer.print(out);
    }

    private void fallar(PrintWriter writer, JsonObject out, String mensaje) {
        out.addProperty("success", false);
        out.addProperty("message", mensaje);
        writer.print(out);
    }

    private int parseIntSeguro(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private String limpiar(HttpServletRequest request, String param) {
        String v = request.getParameter(param);
        if (v == null) {
            return "";
        }
        return v.trim().replaceAll("[\\u0000-\\u001F]", "");
    }
}
