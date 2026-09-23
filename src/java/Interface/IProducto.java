
package Interface;

import Model.Producto;
import java.util.List;

public interface IProducto {
    
    public List<Producto> lista();
    public boolean insertar(Producto producto);
    public boolean update(Producto producto);
    public Producto SearchById(int id);
    public boolean delete(int id);
    public boolean updateStock(int id, int stock);
    public List<Producto> SearchByStockMinimo(int stockMinimo);
    public List<Producto> SearchWithStock();
    public List<Producto> SearchByCategoriaId(int idCategoria);
    public Producto SearchByNombreExact(String nombre);
}
