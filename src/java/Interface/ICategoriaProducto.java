
package Interface;

import Model.CategoriaProducto;
import java.util.List;

public interface ICategoriaProducto {
    
    public List<CategoriaProducto> lista();
    public boolean insertar(CategoriaProducto categoria);
    public boolean update(CategoriaProducto categoria);
    public CategoriaProducto SearchById(int id);
    public boolean delete(int id);
    
}
