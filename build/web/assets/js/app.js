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
    const res = await fetch(url, opts);
    return res.json();
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

/* ============================================================
   Sesión global (se llena al llamar verificarSesion).
   Este sitio es exclusivo para administradores: no hay clientes
   con cuenta ni auto-registro.
   ============================================================ */
window.JX_SESSION = { logueado: false, usuario: null, rol: null };

async function verificarSesion() {
    const r = await jxApi('AuthController', 'GET', { action: 'verificar' });
    const userProfile = document.getElementById('user-profile');
    const userName = document.getElementById('user-name');
    const adminNav = document.getElementById('admin-nav-links');

    if (r.success && r.logueado) {
        window.JX_SESSION = { logueado: true, usuario: r.usuario, rol: r.rol };

        if (userProfile) userProfile.classList.remove('d-none');
        if (userName) userName.textContent = r.usuario;
        if (adminNav) adminNav.classList.remove('d-none');
        marcarLinkActivo();
    } else {
        window.JX_SESSION = { logueado: false, usuario: null, rol: null };
        if (userProfile) userProfile.classList.add('d-none');
        if (adminNav) adminNav.classList.add('d-none');
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
    document.querySelectorAll('#admin-nav-links .nav-link').forEach(link => {
        if (link.getAttribute('href') === actual) link.classList.add('active');
    });
}

/* Bloquea el acceso a una página de administración si no hay sesión activa. */
function jxRequireLogin() {
    if (!window.JX_SESSION.logueado) {
        Swal.fire({
            icon: 'info',
            title: 'Inicia sesión',
            text: 'Necesitas iniciar sesión como administrador para continuar.',
            confirmButtonText: 'Ir al login'
        }).then(() => window.location.href = 'index.html');
        return false;
    }
    return true;
}

/* ============================================================
   Formulario de login (vive en index.html, que ahora es la
   pantalla de acceso exclusiva para administradores)
   ============================================================ */
function inicializarEventosAuth() {
    const formLogin = document.getElementById('form-login');
    if (formLogin && !formLogin.dataset.bound) {
        formLogin.dataset.bound = '1';
        formLogin.addEventListener('submit', async (e) => {
            e.preventDefault();
            mostrarPantallaCarga('Iniciando sesión...');
            const fd = new FormData(formLogin);
            const r = await jxApi('AuthController', 'POST', {
                usuario: fd.get('usuario'), password: fd.get('password'), action: 'login'
            });
            if (r.success) {
                await verificarSesion();
                mostrarPantallaCarga('Cargando panel administrativo...');
                window.location.href = 'admin_dashboard.html';
            } else {
                ocultarPantallaCarga();
                jxToast('error', r.message || 'Usuario o contraseña incorrecta');
            }
        });
    }
}

/* Arranque común a toda página: se dispara cuando main.js termina de cargar todo */
document.addEventListener('jx:ready', async () => {
    await verificarSesion();
    inicializarEventosAuth();
    if (typeof jxPageInit === 'function') {
        const resultado = jxPageInit();
        if (resultado instanceof Promise) await resultado;
    }
    ocultarPantallaCarga();
});
