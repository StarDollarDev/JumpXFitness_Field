function badgeEstadoPago(estado) {
    const map = { PENDIENTE: 'bg-warning text-dark', CONFIRMADO: 'bg-success', RECHAZADO: 'bg-danger' };
    return `<span class="badge ${map[estado] || 'bg-secondary'}">${estado}</span>`;
}

async function cargarMiPlan() {
    const box = document.getElementById('plan-actual-box');
    const r = await jxApi('MiPlanController', 'GET', {});

    if (!r.success) {
        box.innerHTML = `<p class="text-danger mb-0">${r.message || 'No se pudo cargar tu plan.'}</p>`;
        return;
    }
    if (!r.tienePlan) {
        box.innerHTML = `<p class="text-muted mb-0">Todavía no tienes un plan contratado. Elige uno abajo y paga con Yape para activarlo.</p>`;
        return;
    }

    const vencido = r.vencido;
    const dias = r.diasRestantes;
    box.innerHTML = `
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
            <div>
                <div class="text-muted small">Plan actual</div>
                <div class="fs-4 fw-bold">${r.nombrePlan}</div>
                <div class="text-muted small">Desde ${r.fechaInicio} · Vence ${r.fechaVencimiento}</div>
            </div>
            <span class="badge ${vencido ? 'bg-danger' : (dias <= 5 ? 'bg-warning text-dark' : 'bg-success')} fs-6 px-3 py-2">
                ${vencido ? 'Plan vencido' : dias + ' día(s) restantes'}
            </span>
        </div>
    `;
}

async function cargarPlanesActivos() {
    const select = document.getElementById('pago-plan');
    const r = await jxApi('PlanJumpingController', 'GET', { action: 'listarActivos' });
    const planes = r.data || r.planes || [];
    select.innerHTML = '<option value="" selected disabled>Selecciona un plan...</option>'
        + planes.map(p => `<option value="${p.id_plan}">${p.nombre} — ${jxMoney(p.precio)}</option>`).join('');
}

async function cargarMisPagos() {
    const tbody = document.getElementById('tabla-mis-pagos');
    const r = await jxApi('PagoYapeController', 'GET', { action: 'misPagos' });
    const pagos = r.data || [];
    if (!r.success || pagos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted">Aún no tienes solicitudes.</td></tr>';
        return;
    }
    tbody.innerHTML = pagos.map(p => `
        <tr>
            <td>${new Date(p.fechaSolicitud).toLocaleDateString('es-PE')}</td>
            <td>${p.plan ? p.plan.nombre : '-'}</td>
            <td>${jxMoney(p.monto)}</td>
            <td>${p.codigoOperacion}</td>
            <td>${badgeEstadoPago(p.estado)}</td>
        </tr>
    `).join('');
}

function inicializarFormPagoYape() {
    const form = document.getElementById('form-pago-yape');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        form.classList.add('was-validated');
        if (!form.checkValidity()) return;

        const boton = document.getElementById('pago-submit');
        const alerta = document.getElementById('pago-alert');
        boton.disabled = true;
        const original = boton.innerHTML;
        boton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Enviando...';
        alerta.className = 'alert d-none';

        const r = await jxApi('PagoYapeController', 'POST', {
            action: 'solicitar',
            idPlan: document.getElementById('pago-plan').value,
            celularPagador: document.getElementById('pago-celular').value.trim(),
            codigoOperacion: document.getElementById('pago-codigo').value.trim()
        });

        alerta.className = 'alert ' + (r.success ? 'alert-success' : 'alert-danger');
        alerta.textContent = r.message;
        alerta.classList.remove('d-none');

        if (r.success) {
            form.reset();
            form.classList.remove('was-validated');
            await cargarMisPagos();
        }
        boton.disabled = false;
        boton.innerHTML = original;
    });
}

async function jxPageInit() {
    if (!jxRequireLogin(['CLIENTE'])) return;
    inicializarFormPagoYape();
    await Promise.all([cargarMiPlan(), cargarPlanesActivos(), cargarMisPagos()]);
}
