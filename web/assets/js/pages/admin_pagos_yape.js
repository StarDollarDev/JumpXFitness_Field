async function cargarPagosPendientes() {
    const r = await jxApi('PagoYapeController', 'GET', { action: 'pendientes' });
    const pagos = r.data || [];
    const tbody = document.querySelector('#tabla-pagos-yape tbody');

    tbody.innerHTML = pagos.map(p => `
        <tr>
            <td>${new Date(p.fechaSolicitud).toLocaleString('es-PE')}</td>
            <td>${p.cliente && p.cliente.persona ? (p.cliente.persona.nombre + ' ' + p.cliente.persona.apellido) : '-'}</td>
            <td>${p.plan ? p.plan.nombre : '-'}</td>
            <td>${jxMoney(p.monto)}</td>
            <td>${p.celularPagador}</td>
            <td>${p.codigoOperacion}</td>
            <td class="text-nowrap">
                <button class="btn btn-sm btn-success me-1" onclick="confirmarPago(${p.idPago})"><i class="bi bi-check-lg"></i> Confirmar</button>
                <button class="btn btn-sm btn-outline-danger" onclick="rechazarPago(${p.idPago})"><i class="bi bi-x-lg"></i> Rechazar</button>
            </td>
        </tr>
    `).join('');

    if ($.fn.DataTable.isDataTable('#tabla-pagos-yape')) {
        $('#tabla-pagos-yape').DataTable().destroy();
    }
    $('#tabla-pagos-yape').DataTable({ order: [[0, 'asc']], language: { url: 'https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json' } });
}

async function confirmarPago(idPago) {
    const conf = await Swal.fire({
        icon: 'question', title: '¿Confirmar este pago?',
        text: 'Se extenderá la vigencia del cliente de inmediato.',
        showCancelButton: true, confirmButtonText: 'Sí, confirmar'
    });
    if (!conf.isConfirmed) return;

    const r = await jxApi('PagoYapeController', 'POST', { action: 'confirmar', idPago });
    jxToast(r.success ? 'success' : 'error', r.message);
    if (r.success) await cargarPagosPendientes();
}

async function rechazarPago(idPago) {
    const { value: motivo, isConfirmed } = await Swal.fire({
        icon: 'warning', title: 'Rechazar pago',
        input: 'text', inputLabel: 'Motivo (opcional)',
        showCancelButton: true, confirmButtonText: 'Rechazar'
    });
    if (!isConfirmed) return;

    const r = await jxApi('PagoYapeController', 'POST', { action: 'rechazar', idPago, motivo: motivo || '' });
    jxToast(r.success ? 'success' : 'error', r.message);
    if (r.success) await cargarPagosPendientes();
}

async function jxPageInit() {
    if (!jxRequireLogin()) return; // por defecto exige rol ADMIN
    await cargarPagosPendientes();
}
