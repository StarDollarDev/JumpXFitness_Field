package Util;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite simple de solicitudes por clave (normalmente la IP) en memoria.
 * Protege los formularios públicos (leads y reclamos) contra spam, que
 * además dispararía notificaciones al Telegram/correo del propietario.
 */
public final class RateLimiter {

    private static final ConcurrentHashMap<String, ArrayDeque<Long>> HITS = new ConcurrentHashMap<>();

    private RateLimiter() {
    }

    /** @return true si la solicitud puede continuar; false si superó el límite. */
    public static boolean permitir(String clave, int maximo, long ventanaMs) {
        long ahora = System.currentTimeMillis();
        if (HITS.size() > 5000) {
            HITS.clear(); // evita crecimiento sin límite ante ataques
        }
        ArrayDeque<Long> cola = HITS.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (cola) {
            while (!cola.isEmpty() && ahora - cola.peekFirst() > ventanaMs) {
                cola.pollFirst();
            }
            if (cola.size() >= maximo) {
                return false;
            }
            cola.addLast(ahora);
            return true;
        }
    }
}
