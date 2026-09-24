/* ============================================================
   Helpers genéricos
   ============================================================ */
async function jxApi(controller, method, params = {}) {
    let url = controller;
    const opts = { method };
    if (method === 'GET') {
        const qs = new URLSearchParams(params).toString();
        if (qs) url += '?' + qs;
    } else {
        opts.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        opts.body = new URLSearchParams(params).toString();
    }
    try {
        const res = await fetch(url, opts);
        const texto = await res.text();
        try {
            return JSON.parse(texto);
        } catch (e) {
            // El servidor no devolvió JSON válido (normalmente un error 500 de Java
            // con su stack trace en HTML). Lo mostramos igual en vez de fallar mudo.
            console.error('Respuesta no-JSON de ' + controller + ':', texto);
            return { success: false, message: 'El servidor respondió con un error inesperado (revisa la consola de Tomcat/NetBeans).' };
        }
    } catch (e) {
        console.error('Error de red llamando a ' + controller + ':', e);
        return { success: false, message: 'No se pudo conectar con el servidor.' };
    }
}

function jxMoney(n) {
    return 'S/ ' + Number(n || 0).toFixed(2);
}

function jxToast(icon, title) {
    Swal.fire({ icon, title, toast: true, position: 'top-end', showConfirmButton: false, timer: 2600 });
}

/* Pantalla de carga a pantalla completa: tapa la página mientras se
   autentica o mientras la página trae sus datos iniciales. */
function mostrarPantallaCarga(texto) {
    const p = document.querySelector('#jx-loading-overlay p');
    if (p && texto) p.textContent = texto;
    document.body.classList.add('jx-show-loading');
}
function ocultarPantallaCarga() {
    document.body.classList.remove('jx-show-loading');
}

/* Adónde debe ir cada rol después de iniciar sesión, o cuando intenta
   entrar a una página que no le corresponde. */
const JX_HOME_POR_ROL = { ADMIN: 'admin_dashboard.html', CLIENTE: 'cliente_dashboard.html' };

/* ============================================================
   Sesión global (se llena al llamar verificarSesion). Ahora hay dos
   roles: ADMIN (panel administrativo completo) y CLIENTE (solo su
   propio plan y pagos).
   ============================================================ */
window.JX_SESSION = { logueado: false, usuario: null, rol: null };

async function verificarSesion() {
    const r = await jxApi('AuthController', 'GET', { action: 'verificar' });
    const userProfile = document.getElementById('user-profile');
    const userName = document.getElementById('user-name');
    const adminNav = document.getElementById('admin-nav-links');
    const clienteNav = document.getElementById('cliente-nav-links');

    if (r.success && r.logueado) {
        window.JX_SESSION = { logueado: true, usuario: r.usuario, rol: r.rol };

        if (userProfile) userProfile.classList.remove('d-none');
        if (userName) userName.textContent = r.usuario;

        // Cada rol ve SOLO su propio menú lateral.
        if (adminNav) adminNav.classList.toggle('d-none', r.rol !== 'ADMIN');
        if (clienteNav) clienteNav.classList.toggle('d-none', r.rol !== 'CLIENTE');

        marcarLinkActivo();
    } else {
        window.JX_SESSION = { logueado: false, usuario: null, rol: null };
        if (userProfile) userProfile.classList.add('d-none');
        if (adminNav) adminNav.classList.add('d-none');
        if (clienteNav) clienteNav.classList.add('d-none');
    }
}

function logout() {
    jxApi('AuthController', 'POST', { action: 'logout' }).then(() => {
        window.location.href = 'index.html';
    });
}

function toggleSidebar() {
    document.getElementById('jxSidebar')?.classList.toggle('show');
    document.getElementById('jx-sidebar-backdrop')?.classList.toggle('show');
}

window.addEventListener('resize', () => {
    if (window.innerWidth >= 992) {
        document.getElementById('jxSidebar')?.classList.remove('show');
        document.getElementById('jx-sidebar-backdrop')?.classList.remove('show');
    }
});

/* Resalta el link del sidebar que corresponde a la página actual */
function marcarLinkActivo() {
    const actual = window.location.pathname.split('/').pop();
    document.querySelectorAll('#admin-nav-links .nav-link, #cliente-nav-links .nav-link').forEach(link => {
        if (link.getAttribute('href') === actual) link.classList.add('active');
    });
}

/* Control de acceso por rol (RBAC) para páginas protegidas.
   Por defecto exige rol ADMIN (así todas las páginas admin_*.js que ya
   llamaban jxRequireLogin() sin argumentos quedan protegidas también por
   rol, sin tener que tocar cada una de ellas).
   Si hay sesión pero con un rol distinto al permitido, no se manda al
   login: se manda a SU propio panel, porque sí está autenticado, solo
   que no tiene permiso para ver esta sección. */
function jxRequireLogin(rolesPermitidos = ['ADMIN']) {
    if (!window.JX_SESSION.logueado) {
        Swal.fire({
            icon: 'info',
            title: 'Inicia sesión',
            text: 'Necesitas iniciar sesión para continuar.',
            confirmButtonText: 'Ir al login'
        }).then(() => window.location.href = 'login.html');
        return false;
    }
    if (!rolesPermitidos.includes(window.JX_SESSION.rol)) {
        const home = JX_HOME_POR_ROL[window.JX_SESSION.rol] || 'index.html';
        Swal.fire({
            icon: 'warning',
            title: 'No tienes acceso',
            text: 'Tu cuenta no tiene permiso para ver esta sección.',
            confirmButtonText: 'Ir a mi panel'
        }).then(() => window.location.href = home);
        return false;
    }
    return true;
}

/* ============================================================
   Formularios de login y registro (viven en login.html, la pantalla
   de acceso unificada).
   ============================================================ */
/* Toggle simple entre "Iniciar sesión" y "Crear cuenta" en login.html
   (sin componentes de Bootstrap: dos botones y dos paneles). */
function jxCambiarTabAuth(tab) {
    document.getElementById('tab-btn-login')?.classList.toggle('active', tab === 'login');
    document.getElementById('tab-btn-registro')?.classList.toggle('active', tab === 'registro');
    document.getElementById('tab-login')?.classList.toggle('active', tab === 'login');
    document.getElementById('tab-registro')?.classList.toggle('active', tab === 'registro');
}

function inicializarEventosAuth() {
    const formLogin = document.getElementById('form-login');
    if (formLogin && !formLogin.dataset.bound) {
        formLogin.dataset.bound = '1';
        formLogin.addEventListener('submit', async (e) => {
            e.preventDefault();
            try {
                mostrarPantallaCarga('Iniciando sesión...');
                const fd = new FormData(formLogin);
                const r = await jxApi('AuthController', 'POST', {
                    usuario: fd.get('usuario'), password: fd.get('password'), action: 'login'
                });
                if (r.success) {
                    await verificarSesion();
                    mostrarPantallaCarga('Cargando tu panel...');
                    // RBAC: cada rol va a su propio panel, nunca a uno fijo.
                    window.location.href = JX_HOME_POR_ROL[r.rol] || 'index.html';
                } else {
                    ocultarPantallaCarga();
                    jxToast('error', r.message || 'Usuario o contraseña incorrecta');
                }
            } catch (err) {
                console.error('Error en login:', err);
                ocultarPantallaCarga();
                jxToast('error', 'Ocurrió un error inesperado. Revisa la consola.');
            }
        });
    }

    const formRegistro = document.getElementById('form-registro');
    if (formRegistro && !formRegistro.dataset.bound) {
        formRegistro.dataset.bound = '1';
        formRegistro.addEventListener('submit', async (e) => {
            e.preventDefault();
            const boton = formRegistro.querySelector('button[type="submit"]');
            const original = boton.innerHTML;
            try {
                boton.disabled = true;
                boton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creando cuenta...';

                // No se envía ningún campo "rol": el registro público SIEMPRE
                // crea una cuenta de cliente; el backend lo fuerza igual aunque
                // se intentara mandar otra cosa.
                const r = await jxApi('AuthController', 'POST', {
                    action: 'registro',
                    nombre: document.getElementById('reg-nombre').value.trim(),
                    apellido: document.getElementById('reg-apellido').value.trim(),
                    documento: document.getElementById('reg-tipo-doc').value,
                    numeroDoc: document.getElementById('reg-numero-doc').value.trim(),
                    telefono: document.getElementById('reg-telefono').value.trim(),
                    usuario: document.getElementById('reg-usuario').value.trim(),
                    password: document.getElementById('reg-password').value
                });

                if (r.success) {
                    jxToast('success', r.message || 'Cuenta creada. Ya puedes iniciar sesión.');
                    formRegistro.reset();
                    jxCambiarTabAuth('login');
                } else {
                    jxToast('error', r.message || 'No se pudo crear la cuenta.');
                }
            } catch (err) {
                console.error('Error en registro:', err);
                jxToast('error', 'Ocurrió un error inesperado. Revisa la consola.');
            } finally {
                boton.disabled = false;
                boton.innerHTML = original;
            }
        });
    }
}

/* Arranque común a toda página: se dispara cuando main.js termina de cargar todo */
document.addEventListener('jx:ready', async () => {
    try {
        await verificarSesion();
        inicializarEventosAuth();
        if (typeof jxPageInit === 'function') {
            const resultado = jxPageInit();
            if (resultado instanceof Promise) await resultado;
        }
    } catch (err) {
        console.error('Error inicializando la página:', err);
        jxToast('error', 'Ocurrió un error cargando la página. Revisa la consola.');
    } finally {
        ocultarPantallaCarga();
    }
});
