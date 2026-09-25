package Util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

/**
 * Cliente mínimo de la API de Culqi v2 (https://api.culqi.com/v2).
 * Se usa para: cargos (tarjeta y Yape, mismo endpoint) y órdenes
 * (PagoEfectivo — CIP para pagar en agentes, bodegas o banca móvil).
 *
 * La llave secreta NUNCA va en el código ni en el frontend: se lee (en este
 * orden) de variable de entorno, propiedad -D, o
 * ${catalina.base}/conf/jx-notify.properties (clave JX_CULQI_SECRET_KEY).
 *
 * IMPORTANTE (léelo antes de usar en producción): no se probó contra una
 * cuenta real de Culqi, así que la forma general de la petición (endpoint,
 * headers, campos de entrada) es la que documenta Culqi públicamente, pero
 * los nombres exactos de algunos campos de RESPUESTA de /v2/orders (el
 * código CIP, la URL del comprobante) pueden variar. Por eso
 * CulqiClient.crearOrden() devuelve el JSON completo tal cual lo manda
 * Culqi: guárdalo y, en tu primera prueba en modo test, revisa ese JSON
 * para confirmar los nombres de campo exactos.
 */
public final class CulqiClient {

    private static final String BASE_URL = "https://api.culqi.com/v2";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();
    private static final Properties ARCHIVO = cargarArchivo();

    private CulqiClient() {
    }

    public static boolean configurado() {
        return !secretKey().isEmpty();
    }

    /** Crea un cargo (tarjeta o Yape: ambos usan el mismo endpoint y el mismo source_id). */
    public static JsonObject crearCargo(int montoCentimos, String moneda, String descripcion,
                                         String email, String sourceId) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("amount", montoCentimos);
        body.addProperty("capture", true);
        body.addProperty("currency_code", moneda);
        body.addProperty("description", descripcion);
        body.addProperty("email", email);
        body.addProperty("installments", 0);
        body.addProperty("source_id", sourceId);
        return post("/charges", body);
    }

    /** Crea una orden de pago (PagoEfectivo / billeteras / Cuotéalo). Genera un CIP para pagar diferido. */
    public static JsonObject crearOrden(int montoCentimos, String moneda, String descripcion,
                                         String ordenNumero, String nombre, String apellido,
                                         String email, String telefono, long segundosVigencia) throws Exception {
        JsonObject cliente = new JsonObject();
        cliente.addProperty("first_name", nombre);
        cliente.addProperty("last_name", apellido);
        cliente.addProperty("email", email);
        cliente.addProperty("phone_number", telefono);

        JsonObject body = new JsonObject();
        body.addProperty("amount", montoCentimos);
        body.addProperty("currency_code", moneda);
        body.addProperty("description", descripcion);
        body.addProperty("order_number", ordenNumero);
        body.add("client_details", cliente);
        body.addProperty("expiration_date", System.currentTimeMillis() / 1000L + segundosVigencia);
        return post("/orders", body);
    }

    /** Vuelve a consultar una orden por su id. Úsalo SIEMPRE al recibir el webhook en vez de confiar en su body. */
    public static JsonObject obtenerOrden(String idOrden) throws Exception {
        return get("/orders/" + idOrden);
    }

    public static JsonObject obtenerCargo(String idCargo) throws Exception {
        return get("/charges/" + idCargo);
    }

    // ------------------------------------------------------------------

    private static JsonObject post(String path, JsonObject body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Authorization", "Bearer " + secretKey())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        return enviar(req);
    }

    private static JsonObject get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + secretKey())
                .GET()
                .build();
        return enviar(req);
    }

    private static JsonObject enviar(HttpRequest req) throws Exception {
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonObject json;
        try {
            json = JsonParser.parseString(resp.body()).getAsJsonObject();
        } catch (Exception e) {
            throw new IOException("Culqi devolvió una respuesta no-JSON (HTTP " + resp.statusCode() + "): "
                    + recortar(resp.body(), 300));
        }
        json.addProperty("_httpStatus", resp.statusCode());
        if (resp.statusCode() / 100 != 2) {
            String msg = json.has("user_message") ? json.get("user_message").getAsString()
                    : json.has("merchant_message") ? json.get("merchant_message").getAsString()
                    : "Error HTTP " + resp.statusCode();
            throw new CulqiException(msg, json);
        }
        return json;
    }

    public static final class CulqiException extends Exception {
        public final JsonObject respuesta;
        public CulqiException(String mensaje, JsonObject respuesta) {
            super(mensaje);
            this.respuesta = respuesta;
        }
    }

    private static String recortar(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : (s == null ? "" : s);
    }

    private static Properties cargarArchivo() {
        Properties p = new Properties();
        String base = System.getProperty("catalina.base");
        if (base != null) {
            Path f = Paths.get(base, "conf", "jx-notify.properties");
            if (Files.isReadable(f)) {
                try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                    p.load(r);
                } catch (IOException ignored) {
                }
            }
        }
        return p;
    }

    private static String cfg(String clave) {
        String v = System.getenv(clave);
        if (v == null || v.isBlank()) v = System.getProperty(clave);
        if (v == null || v.isBlank()) v = ARCHIVO.getProperty(clave);
        return v == null ? "" : v.trim();
    }

    public static String secretKey() {
        return cfg("JX_CULQI_SECRET_KEY");
    }
}
