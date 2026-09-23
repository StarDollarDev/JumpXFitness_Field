
package Test;

import Util.ConexionSqlSingleton;
import java.sql.*;


public class TestBD {

    
    public static void main(String[] args) {
        TestBD t = new TestBD();
        t.testConexion();
    }
    public void testConexion() {
        ConexionSqlSingleton conn = new ConexionSqlSingleton();
        try {
            Connection connection = conn.getConnection();
            if (connection != null && !connection.isClosed()) {
                System.out.println("Conexion satisfactoria!!!");
            } else {
                System.out.println("No se puede establecer conexion");
            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    
}
