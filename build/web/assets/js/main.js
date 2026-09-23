async function loadComponent(id, file) {
    const response = await fetch(file, { cache: 'no-store' });
    const data = await response.text();
    document.getElementById(id).innerHTML = data;
}

function loadScript(src) {
    return new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = src;
        script.onload = resolve;
        script.onerror = reject;
        document.body.appendChild(script);
    });
}

async function initJumpx() {
    try {
        await loadComponent('head-placeholder', 'head.html');
        await loadComponent('header-placeholder', 'header.html');
        await loadComponent('footer-placeholder', 'footer.html');

        await loadScript('https://code.jquery.com/jquery-3.6.0.min.js');
        await loadScript('https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js');
        await loadScript('https://cdn.jsdelivr.net/npm/sweetalert2@11');
        await loadScript('https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js');
        await loadScript('https://cdn.datatables.net/1.13.6/js/dataTables.bootstrap5.min.js');

        await loadScript('assets/js/app.js');

        // Avisamos a la página actual (index, admin_planes, etc.) que ya puede
        // usar el header, el DOM y todas las librerías con seguridad.
        document.dispatchEvent(new CustomEvent('jx:ready'));
    } catch (e) {
        console.error("Error al cargar la aplicación", e);
    }
}

initJumpx();
