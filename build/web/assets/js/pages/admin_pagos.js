function badgeEstado(estado) {
    const map = { PENDIENTE: 'bg-warning text-dark', PAGADO: 'bg-success', RECHAZADO: 'bg-danger', EXPIRADO: 'bg-secondary' };
    return `<span class="badge ${map[estado] || 'bg-secondary'}">${estado}</span>`;
}

async function cargarHistorialPagos() {
    const r = await jxApi('PagoCulqiController', 'GET', { action: 'historial' });
    const pagos = r.data || [];
    const tbody = document.querySelector('#tabla-pagos tbody');

    tbody.innerHTML = pagos.map(p => `
        <tr style="cursor:pointer" onclick='verDetallePago(${JSON.stringify(p.respuestaJson || "{}")})'>
            <td>${new Date(p.fechaCreacion).toLocaleString('es-PE')}</td>
            <td>${p.cliente && p.cliente.persona ? (p.cliente.persona.nombre + ' ' + p.cliente.persona.apellido) : '-'}</td>
            <td>${p.plan ? p.plan.nombre : '-'}</td>
            <td>${p.metodo}</td>
            <td>${jxMoney(p.monto)}</td>
            <td>${badgeEstado(p.estado)}</td>
            <td><i class="bi bi-eye text-muted"></i></td>
        </tr>
    `).join('');

    if ($.fn.DataTable.isDataTable('#tabla-pagos')) {
        $('#tabla-pagos').DataTable().destroy();
    }
    $('#tabla-pagos').DataTable({ order: [[0, 'desc']], language: { url: 'https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json' } });
}

function verDetallePago(respuestaJsonTexto) {
    let bonito = respuestaJsonTexto;
    try { bonito = JSON.stringify(JSON.parse(respuestaJsonTexto), null, 2); } catch (e) { /* se muestra tal cual */ }
    Swal.fire({
        title: 'Respuesta de Culqi',
        html: `<pre class="text-start small" style="max-height:400px;overflow:auto;">${bonito.replace(/</g, '&lt;')}</pre>`,
        width: 600
    });
}

async function jxPageInit() {
    if (!jxRequireLogin()) return; // por defecto exige rol ADMIN
    await cargarHistorialPagos();
}
