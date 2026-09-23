
package Model;

public class Cliente extends Persona{
    
    private int id_cliente;
    private Persona persona;

    public Cliente() {
    }

    public Cliente(int id_cliente, Persona persona) {
        this.id_cliente = id_cliente;
        this.persona = persona;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
    
}
