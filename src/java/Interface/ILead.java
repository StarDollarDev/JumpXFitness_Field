package Interface;

import Model.Lead;
import java.util.List;

public interface ILead {
    boolean insertar(Lead lead);
    List<Lead> lista();
}
