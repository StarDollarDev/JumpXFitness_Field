
package Controllers;

import Dao.UsuarioDaoImpl;
import Interface.IUsuario;
import Model.Usuario;
import Util.AuditoriaHelper;
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
 * Autenticación exclusiva para administradores.
 * Ya no existe auto-registro: las cuentas de administrador solo se crean
 * desde UsuarioController por otro administrador ya autenticado.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/AuthController"})
public class AuthController extends HttpServlet {

    private final IUsuario uDao = new UsuarioDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        try (PrintWriter out = response.getWriter()) {

            if ("verificar".equals(action)) {
                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute("usuario") != null) {
                    Usuario us = (Usuario) session.getAttribute("usuario");
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("logueado", true);
                    jsonResponse.addProperty("usuario", us.getUsuario());
                    jsonResponse.addProperty("rol", us.getRol().name());
                } else {
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("logueado", false);
                }
                out.print(jsonResponse.toString());
            }
        } catch (Exception e) {
            response.setStatus(500);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
            response.getWriter().print(jsonResponse.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        try (PrintWriter out = response.getWriter()) {

            if ("login".equals(action)) {
                String user = request.getParameter("usuario");
                String pass = request.getParameter("password");

                // validate() compara con BCrypt internamente: se envia en texto plano.
                Usuario us = uDao.validate(user, pass);

                if (us != null && us.getUsuario() != null) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("usuario", us);
                    session.setMaxInactiveInterval(1800);

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Inicio de sesión exitoso");
                    jsonResponse.addProperty("usuario", us.getUsuario());
                    jsonResponse.addProperty("rol", us.getRol().name());
                    jsonResponse.add("userData", gson.toJsonTree(us));

                    AuditoriaHelper.registrar(request, "LOGIN", "usuario", us.getId_usuario(),
                            "Inicio de sesión exitoso de '" + us.getUsuario() + "'");
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Usuario o contraseña incorrecta");

                    AuditoriaHelper.registrarConUsuario(request, null, user, "LOGIN_FALLIDO", "usuario", null,
                            "Intento de inicio de sesión fallido para '" + user + "'");
                }
                out.print(jsonResponse.toString());

            } else if ("logout".equals(action)) {
                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute("usuario") != null) {
                    Usuario us = (Usuario) session.getAttribute("usuario");
                    AuditoriaHelper.registrar(request, "LOGOUT", "usuario", us.getId_usuario(),
                            "Cierre de sesión de '" + us.getUsuario() + "'");
                }
                if (session != null) {
                    session.invalidate();
                }
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Sesión cerrada exitosamente");

                out.print(jsonResponse.toString());
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Acción no válida");
                out.print(jsonResponse.toString());
            }
        } catch (Exception e) {
            response.setStatus(500);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
            response.getWriter().print(jsonResponse.toString());
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
