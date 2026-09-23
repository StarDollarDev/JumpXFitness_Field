package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto único de conexión a Oracle para toda la app.
 *
 * El problema del diseño anterior: guardaba la Connection en un campo
 * static y la reutilizaba para siempre sin comprobar si seguía viva.
 * Si Oracle la cerraba por inactividad, o el servicio se reiniciaba,
 * TODAS las consultas siguientes fallaban con el mismo error genérico
 * ("Conexion fallida") sin decir nunca la causa real, y sin intentar
 * reconectar jamás.
 *
 * Este archivo mantiene la misma idea simple (una sola conexión
 * compartida, sin pool) pero:
 *   1) antes de entregar la conexión, valida que siga viva y, si no,
 *      la vuelve a abrir sola (self-healing);
 *   2) si Oracle rechaza la conexión, muestra el error real de Oracle
 *      en consola (código ORA-xxxx) en vez de taparlo.
 */
public class ConexionSqlSingleton {

    private static Connection connection;

    // Si tu Oracle es 18c/19c/21c XE, puede que la base ya no use SID="XE"
    // sino un "service name" tipo "XEPDB1". Si ves ORA-12505 o ORA-12514,
    // cambia SID por el service name correcto (pídele a tu Oracle:
    // "SELECT name FROM v$pdbs;" para confirmarlo).
    private static final String HOST = "localhost";
    private static final String PORT = "1521";
    private static final String SID = "XE";
    private static final String USER = "jump_fitness";
    private static final String PASSWORD = "jump123";

    private static final String URL = "jdbc:oracle:thin:@" + HOST + ":" + PORT + ":" + SID;

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            // Si esto se imprime, el driver de Oracle (ojdbc) no está
            // realmente en el classpath desplegado (revisa que sea un
            // .jar válido, no un .zip, y que esté en WEB-INF/lib).
            System.out.println("No se encontró el driver de Oracle (oracle.jdbc.driver.OracleDriver): " + e.getMessage());
        }

        // Cierra la conexión ordenadamente cuando el servidor se apaga.
        Runtime.getRuntime().addShutdownHook(new Thread(ConexionSqlSingleton::cerrar));
    }

    public ConexionSqlSingleton() {
    }

    /**
     * Devuelve una conexión lista para usar. Si la que teníamos guardada
     * ya no sirve (cerrada, caída, Oracle reiniciado), abre una nueva
     * automáticamente en vez de seguir devolviendo/reintentando la misma
     * conexión muerta.
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(3)) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return connection;

        } catch (SQLException e) {
            // Se imprime el error real de Oracle (ORA-xxxx) para poder
            // diagnosticar: usuario/clave incorrectos, listener caído,
            // SID/service name equivocado, máximo de sesiones alcanzado, etc.
            System.out.println("Error de conexion a Oracle [" + e.getErrorCode() + "]: " + e.getMessage());
            throw new RuntimeException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }

    private static void cerrar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexion: " + e.getMessage());
        }
    }
}
