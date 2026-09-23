package Model;

import java.security.MessageDigest;

public class Usuario {

    private int id_usuario;
    private String usuario;
    private String contraseña;
    private Rol rol;
    private Persona persona;

    public Usuario() {
    }

    public Usuario(int id_usuario, String usuario, String contraseña, Rol rol, Persona persona) {
        this.id_usuario = id_usuario;
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.rol = rol;
        this.persona = persona;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String HashPassword(String contraseña) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(contraseña, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }
    public static boolean checkPassword(String contraseñaPlano, String hashGuardado) {
        return org.mindrot.jbcrypt.BCrypt.checkpw(contraseñaPlano, hashGuardado);
    }
}
