function jxPageInit() {
    // Si ya hay una sesión de administrador activa, saltamos directo al panel.
    if (window.JX_SESSION.logueado) {
        mostrarPantallaCarga('Cargando panel administrativo...');
        window.location.href = 'admin_dashboard.html';
    }
}
