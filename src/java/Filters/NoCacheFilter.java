package Filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Mientras el proyecto está en desarrollo, evita que el navegador guarde en
 * caché las páginas HTML, JS o CSS de la app: cada carga trae siempre la
 * versión más reciente desplegada, sin depender de que el usuario haga un
 * refresco forzado (Ctrl+Shift+R) cada vez que se sube un cambio.
 *
 * Antes de pasar esto a producción real, conviene quitar este filtro (o
 * relajarlo) para que el navegador SÍ pueda cachear los recursos estáticos
 * y la app cargue más rápido.
 */
@WebFilter("/*")
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        chain.doFilter(req, res);
    }
}
