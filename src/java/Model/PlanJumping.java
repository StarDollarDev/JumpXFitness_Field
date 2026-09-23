package Model;

public class PlanJumping {

    private int id_plan;
    private String nombre;
    private double precio;
    private int cantidadPersonas;
    private int diasVigencia;
    private boolean activo;
    private String imagen;

    public PlanJumping() {
    }

    public PlanJumping(int id_plan, String nombre, double precio, int cantidadPersonas, int diasVigencia, boolean activo) {
        this.id_plan = id_plan;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidadPersonas = cantidadPersonas;
        this.diasVigencia = diasVigencia;
        this.activo = activo;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public int getId_plan() {
        return id_plan;
    }

    public void setId_plan(int id_plan) {
        this.id_plan = id_plan;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(int cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public int getDiasVigencia() {
        return diasVigencia;
    }

    public void setDiasVigencia(int diasVigencia) {
        this.diasVigencia = diasVigencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
}
