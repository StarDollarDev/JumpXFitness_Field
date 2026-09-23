package Controllers;

import Dao.PersonaDaoImpl;
import Dao.UsuarioDaoImpl;
import Interface.IPersona;
import Interface.IUsuario;
import Model.Persona;
import Model.Rol;
import Model.Usuario;
import Util.AuditoriaHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet(name = "UsuarioController", urlPatterns = {"/UsuarioController"})
public class UsuarioController extends HttpServlet {

    private final IUsuario uDao = new UsuarioDaoImpl();
    private final IPersona pDao = new PersonaDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== USUARIO CONTROLLER (GET) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {

            if ("listar".equals(action)) {
                List<Usuario> lista = uDao.lista();
                JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Usuarios listados correctamente");
                jsonResponse.add("data", jsonArray);
                out.print(jsonResponse.toString());

            } else if ("buscar".equals(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    Usuario u = uDao.SearchById(id);
                    if (u != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(u));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Usuario no encontrado");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID requerido");
                }
                out.print(jsonResponse.toString());

            } else if ("buscarPorUsuario".equals(action)) {
                String username = request.getParameter("usuario");
                if (username != null && !username.trim().isEmpty()) {
                    Usuario u = uDao.SearchByUsername(username.trim());
                    if (u != null) {
                        jsonResponse.addProperty("success", true);
                        jsonResponse.add("data", gson.toJsonTree(u));
                    } else {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "Usuario no encontrado");
                    }
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Nombre de usuario requerido");
                }
                out.print(jsonResponse.toString());

            } else if ("listarPorRol".equals(action)) {
                String rol = request.getParameter("rol");
                if (rol != null && !rol.isEmpty()) {
                    List<Usuario> lista = uDao.SearchByRol(rol);
                    JsonArray jsonArray = gson.toJsonTree(lista).getAsJsonArray();

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Usuarios por rol listados correctamente");
                    jsonResponse.add("data", jsonArray);
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Rol requerido (ADMIN o CLIENTE)");
                }
                out.print(jsonResponse.toString());

            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Acción no válida: " + action);
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
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        JsonObject jsonResponse = new JsonObject();

        System.out.println("=== USUARIO CONTROLLER (POST) ===");
        System.out.println("Action: " + action);

        try (PrintWriter out = response.getWriter()) {

            if ("insertar".equals(action)) {
                String usuario = request.getParameter("usuario");
                String contraseña = request.getParameter("password");
                String rol = request.getParameter("rol");

                String nombre = request.getParameter("nombre");
                String apellido = request.getParameter("apellido");
                String documento = request.getParameter("documento");
                String numeroDoc = request.getParameter("numeroDoc");
                String telefono = request.getParameter("telefono");

                // Validar campos requeridos
                if (usuario == null || usuario.trim().isEmpty()
                        || contraseña == null || contraseña.trim().isEmpty()
                        || rol == null || rol.trim().isEmpty()
                        || nombre == null || nombre.trim().isEmpty()
                        || apellido == null || apellido.trim().isEmpty()
                        || documento == null || documento.trim().isEmpty()
                        || numeroDoc == null || numeroDoc.trim().isEmpty()) {

                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Todos los campos son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }

                // Verificar si el usuario ya existe
                Usuario usuarioExistente = uDao.SearchByUsername(usuario.trim());
                if (usuarioExistente != null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "El nombre de usuario ya está en uso");
                    out.print(jsonResponse.toString());
                    return;
                }

                try {
                    Persona persona = new Persona();
                    persona.setNombre(nombre.trim());
                    persona.setApellido(apellido.trim());
                    persona.setDocumento(documento.trim());
                    persona.setNumeroDoc(numeroDoc.trim());
                    persona.setTelefono(telefono != null ? telefono.trim() : "");

                    Usuario u = new Usuario();
                    u.setUsuario(usuario.trim());

                    // HASHEAR LA CONTRASEÑA ANTES DE GUARDAR
                    String passHash = u.HashPassword(contraseña.trim());
                    u.setContraseña(passHash);

                    u.setRol(Rol.valueOf(rol.toUpperCase()));
                    u.setPersona(persona);

                    boolean resultado = uDao.insertar(u);

                    jsonResponse.addProperty("success", resultado);
                    if (resultado) {
                        jsonResponse.addProperty("message", "Usuario insertado correctamente");
                        jsonResponse.addProperty("id", u.getId_usuario());
                        jsonResponse.add("data", gson.toJsonTree(u));

                        AuditoriaHelper.registrar(request, "INSERT", "usuario", u.getId_usuario(),
                                "Alta de administrador '" + u.getUsuario() + "'");
                    } else {
                        jsonResponse.addProperty("message", "Error al insertar usuario");
                    }
                } catch (IllegalArgumentException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Rol inválido. Use ADMIN");
                }
                out.print(jsonResponse.toString());

            } else if ("actualizar".equals(action)) {
                String idParam = request.getParameter("id");
                String usuario = request.getParameter("usuario");
                String contraseña = request.getParameter("password");
                String rol = request.getParameter("rol");

                String nombre = request.getParameter("nombre");
                String apellido = request.getParameter("apellido");
                String documento = request.getParameter("documento");
                String numeroDoc = request.getParameter("numeroDoc");
                String telefono = request.getParameter("telefono");

                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del usuario requerido");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);
                Usuario u = uDao.SearchById(id);

                if (u == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Usuario no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }

                try {
                    // Actualizar datos del usuario
                    if (usuario != null && !usuario.trim().isEmpty()) {
                        Usuario usuarioExistente = uDao.SearchByUsername(usuario.trim());
                        if (usuarioExistente != null && usuarioExistente.getId_usuario() != id) {
                            jsonResponse.addProperty("success", false);
                            jsonResponse.addProperty("message", "El nombre de usuario ya está en uso");
                            out.print(jsonResponse.toString());
                            return;
                        }
                        u.setUsuario(usuario.trim());
                    }

                    if (contraseña != null && !contraseña.trim().isEmpty()) {
                        // HASHEAR LA NUEVA CONTRASEÑA
                        String passHash = u.HashPassword(contraseña.trim());
                        u.setContraseña(passHash);
                    }

                    if (rol != null && !rol.trim().isEmpty()) {
                        u.setRol(Rol.valueOf(rol.toUpperCase()));
                    }

                    // Actualizar datos de la persona
                    Persona persona = u.getPersona();
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        persona.setNombre(nombre.trim());
                    }
                    if (apellido != null && !apellido.trim().isEmpty()) {
                        persona.setApellido(apellido.trim());
                    }
                    if (documento != null && !documento.trim().isEmpty()) {
                        persona.setDocumento(documento.trim());
                    }
                    if (numeroDoc != null && !numeroDoc.trim().isEmpty()) {
                        persona.setNumeroDoc(numeroDoc.trim());
                    }
                    if (telefono != null && !telefono.trim().isEmpty()) {
                        persona.setTelefono(telefono.trim());
                    }

                    boolean resultado = uDao.update(u);

                    jsonResponse.addProperty("success", resultado);
                    jsonResponse.addProperty("message", resultado ? "Usuario actualizado correctamente" : "Error al actualizar usuario");
                    if (resultado) {
                        jsonResponse.add("data", gson.toJsonTree(u));
                        AuditoriaHelper.registrar(request, "UPDATE", "usuario", u.getId_usuario(),
                                "Actualización de datos del administrador '" + u.getUsuario() + "'");
                    }
                } catch (IllegalArgumentException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Rol inválido. Use ADMIN");
                }
                out.print(jsonResponse.toString());

            } else if ("cambiarRol".equals(action)) {
                String idParam = request.getParameter("id");
                String rol = request.getParameter("rol");

                if (idParam == null || idParam.isEmpty() || rol == null || rol.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID y rol son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);
                Usuario u = uDao.SearchById(id);

                if (u == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Usuario no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }

                try {
                    u.setRol(Rol.valueOf(rol.toUpperCase()));
                    boolean resultado = uDao.update(u);

                    jsonResponse.addProperty("success", resultado);
                    jsonResponse.addProperty("message", resultado ? "Rol actualizado correctamente" : "Error al actualizar rol");
                    if (resultado) {
                        jsonResponse.addProperty("rol", u.getRol().name());
                        jsonResponse.add("data", gson.toJsonTree(u));
                        AuditoriaHelper.registrar(request, "UPDATE", "usuario", u.getId_usuario(),
                                "Cambio de rol de '" + u.getUsuario() + "' a " + u.getRol().name());
                    }
                } catch (IllegalArgumentException e) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Rol inválido. Use ADMIN");
                }
                out.print(jsonResponse.toString());

            } else if ("cambiarPassword".equals(action)) {
                String idParam = request.getParameter("id");
                String contraseñaActual = request.getParameter("passwordActual");
                String contraseñaNueva = request.getParameter("passwordNueva");

                if (idParam == null || idParam.isEmpty()
                        || contraseñaActual == null || contraseñaActual.isEmpty()
                        || contraseñaNueva == null || contraseñaNueva.isEmpty()) {

                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID, contraseña actual y nueva contraseña son requeridos");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);
                Usuario u = uDao.SearchById(id);

                if (u == null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Usuario no encontrado");
                    out.print(jsonResponse.toString());
                    return;
                }

                // HASHEAR LA CONTRASEÑA ACTUAL PARA COMPARAR
                String passHashActual = u.HashPassword(contraseñaActual);

                if (!u.getContraseña().equals(passHashActual)) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Contraseña actual incorrecta");
                    out.print(jsonResponse.toString());
                    return;
                }

                // HASHEAR LA NUEVA CONTRASEÑA
                String passHashNueva = u.HashPassword(contraseñaNueva);
                u.setContraseña(passHashNueva);

                boolean resultado = uDao.update(u);

                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Contraseña actualizada correctamente" : "Error al actualizar contraseña");
                if (resultado) {
                    AuditoriaHelper.registrar(request, "UPDATE", "usuario", u.getId_usuario(),
                            "Cambio de contraseña del administrador '" + u.getUsuario() + "'");
                }
                out.print(jsonResponse.toString());

            } else if ("eliminar".equals(action)) {
                String idParam = request.getParameter("id");

                if (idParam == null || idParam.isEmpty()) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "ID del usuario requerido");
                    out.print(jsonResponse.toString());
                    return;
                }

                int id = Integer.parseInt(idParam);

                // Verificar que no sea el último ADMIN
                Usuario u = uDao.SearchById(id);
                if (u != null && u.getRol() == Rol.ADMIN) {
                    List<Usuario> admins = uDao.SearchByRol("ADMIN");
                    if (admins.size() <= 1) {
                        jsonResponse.addProperty("success", false);
                        jsonResponse.addProperty("message", "No se puede eliminar el último administrador");
                        out.print(jsonResponse.toString());
                        return;
                    }
                }

                boolean resultado = uDao.delete(id);

                jsonResponse.addProperty("success", resultado);
                jsonResponse.addProperty("message", resultado ? "Usuario eliminado correctamente" : "Error al eliminar usuario");
                if (resultado && u != null) {
                    AuditoriaHelper.registrar(request, "DELETE", "usuario", id,
                            "Eliminación del administrador '" + u.getUsuario() + "'");
                }
                out.print(jsonResponse.toString());

            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Acción no válida: " + action);
                out.print(jsonResponse.toString());
            }

        } catch (Exception e) {
            response.setStatus(500);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
            response.getWriter().print(jsonResponse.toString());
        }
    }
}
