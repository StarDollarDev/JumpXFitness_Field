package Controllers;

import Dao.ClienteDaoImpl;
import Dao.UsuarioDaoImpl;
import Interface.IUsuario;
import Model.Cliente;
import Model.Persona;
import Model.Rol;
import Model.Usuario;
import Util.AuditoriaHelper;
import Util.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Autenticación unificada: login y registro público en el mismo endpoint.
 *
 * Seguridad del registro: el público SOLO puede crear cuentas de tipo
 * CLIENTE. El rol nunca se lee de lo que manda el navegador; esta clase
 * asigna Rol.CLIENTE de forma fija sin importar qué parámetro llegue, así
 * que no existe ninguna forma de registrarse como ADMIN desde este
 * formulario. Las cuentas ADMIN solo se crean desde UsuarioController por
 * otro administrador ya autenticado.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/AuthController"})
public class AuthController extends HttpServlet {

    private final IUsuario uDao = new UsuarioDaoImpl();
    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final Gson gson = new Gson();

    private static final Pattern USUARIO_VALIDO = Pattern.compile("^[a-zA-Z0-9._-]{4,40}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+() \\-]{7,20}$");

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

                if (!RateLimiter.permitir("login:" + clienteIp(request), 10, 10 * 60 * 1000L)) {
                    response.setStatus(429);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Demasiados intentos. Espera unos minutos e inténtalo de nuevo.");
                    out.print(jsonResponse.toString());
                    return;
                }

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

            } else if ("registro".equals(action)) {
                registrarCliente(request, response, out, jsonResponse);

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
            try {
                response.getWriter().print(gson.toJson(jsonResponse));
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Registro público de CLIENTES. El rol NO se toma de request: siempre
     * es Rol.CLIENTE, sin excepción (ver nota de seguridad de la clase).
     */
    private void registrarCliente(HttpServletRequest request, HttpServletResponse response,
                                   PrintWriter out, JsonObject jsonResponse) {

        if (!RateLimiter.permitir("registro:" + clienteIp(request), 5, 15 * 60 * 1000L)) {
            response.setStatus(429);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Demasiados intentos de registro. Espera unos minutos.");
            out.print(jsonResponse.toString());
            return;
        }

        String usuarioTxt = limpiar(request, "usuario");
        String password = request.getParameter("password") == null ? "" : request.getParameter("password");
        String nombre = limpiar(request, "nombre");
        String apellido = limpiar(request, "apellido");
        String documento = limpiar(request, "documento").toUpperCase(); // DNI | CE
        String numeroDoc = limpiar(request, "numeroDoc");
        String telefono = limpiar(request, "telefono");

        if (!USUARIO_VALIDO.matcher(usuarioTxt).matches()) {
            fallar(out, jsonResponse, "El usuario debe tener entre 4 y 40 caracteres (letras, números, punto, guion o guion bajo).");
            return;
        }
        if (password.length() < 6 || password.length() > 72) {
            fallar(out, jsonResponse, "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (nombre.length() < 2 || nombre.length() > 60 || apellido.length() < 2 || apellido.length() > 60) {
            fallar(out, jsonResponse, "Ingresa tu nombre y apellido.");
            return;
        }
        if (!documento.equals("DNI") && !documento.equals("CE")) {
            fallar(out, jsonResponse, "Selecciona un tipo de documento válido (DNI o CE).");
            return;
        }
        if (numeroDoc.length() < 6 || numeroDoc.length() > 15) {
            fallar(out, jsonResponse, "Ingresa un número de documento válido.");
            return;
        }
        if (!PHONE.matcher(telefono).matches()) {
            fallar(out, jsonResponse, "Ingresa un número de teléfono válido.");
            return;
        }

        if (uDao.SearchByUsername(usuarioTxt) != null) {
            fallar(out, jsonResponse, "Ese nombre de usuario ya está en uso.");
            return;
        }

        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setDocumento(documento);
        persona.setNumeroDoc(numeroDoc);
        persona.setTelefono(telefono);

        Usuario nuevo = new Usuario();
        nuevo.setUsuario(usuarioTxt);
        nuevo.setContraseña(nuevo.HashPassword(password));
        nuevo.setRol(Rol.CLIENTE); // fijo: nunca viene del request
        nuevo.setPersona(persona);

        boolean ok = uDao.insertar(nuevo);
        if (!ok) {
            fallar(out, jsonResponse, "No se pudo completar el registro. Inténtalo nuevamente.");
            return;
        }

        // Todo cliente con cuenta necesita también su fila en `cliente` para
        // poder contratar/renovar planes (registrojumping referencia id_cliente).
        Cliente cliente = clienteDao.crearParaPersonaExistente(nuevo.getPersona().getId_persona());
        if (cliente == null) {
            System.err.println("Aviso: se creó el usuario " + nuevo.getUsuario()
                    + " pero no se pudo crear su fila en 'cliente'.");
        }

        AuditoriaHelper.registrarConUsuario(request, nuevo.getId_usuario(), nuevo.getUsuario(),
                "INSERT", "usuario", nuevo.getId_usuario(),
                "Autorregistro de cliente '" + nuevo.getUsuario() + "'");

        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "¡Cuenta creada! Ya puedes iniciar sesión.");
        out.print(jsonResponse.toString());
    }

    private void fallar(PrintWriter out, JsonObject jsonResponse, String mensaje) {
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", mensaje);
        out.print(jsonResponse.toString());
    }

    private String limpiar(HttpServletRequest request, String param) {
        String v = request.getParameter(param);
        if (v == null) {
            return "";
        }
        return v.trim().replaceAll("[\\u0000-\\u001F]", "");
    }

    private String clienteIp(HttpServletRequest request) {
        String fwd = request.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            return fwd.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
