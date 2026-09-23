
package Interface;

import Model.PlanJumping;
import java.util.List;

public interface IPlanJumping {
    
    public List<PlanJumping> lista();
    public boolean insertar(PlanJumping plan);
    public boolean update(PlanJumping plan);
    public PlanJumping SearchById(int id);
    public boolean delete(int id);
    public List<PlanJumping> listaActivos();
}
