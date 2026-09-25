// Llave PÚBLICA de Culqi (no es secreta, se puede exponer en el frontend).
// Reemplázala por tu llave real: CulqiPanel > Desarrollo > API Keys.
// Empieza con pk_test_... mientras pruebas, y pk_live_... en producción.
const CULQI_PUBLIC_KEY = 'pk_test_REEMPLAZA_ESTA_LLAVE';

let planSeleccionado = null;

function badgeEstadoPago(estado) {
    const map = { PENDIENTE: 'bg-warning text-dark', PAGADO: 'bg-success', RECHAZADO: 'bg-danger', EXPIRADO: 'bg-secondary' };
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
        box.innerHTML = `<p class="text-muted mb-0">Todavía no tienes un plan contratado. Elige uno abajo y paga para activarlo.</p>`;
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
        + planes.map(p => `<option value="${p.id_plan}" data-precio="${p.precio}" data-nombre="${p.nombre}">${p.nombre} — ${jxMoney(p.precio)}</option>`).join('');
}

async function cargarMisPagos() {
    const tbody = document.getElementById('tabla-mis-pagos');
    const r = await jxApi('PagoCulqiController', 'GET', { action: 'misPagos' });
    const pagos = r.data || [];
    if (!r.success || pagos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted">Aún no tienes pagos.</td></tr>';
        return;
    }
    tbody.innerHTML = pagos.map(p => `
        <tr>
            <td>${new Date(p.fechaCreacion).toLocaleDateString('es-PE')}</td>
            <td>${p.plan ? p.plan.nombre : '-'}</td>
            <td>${p.metodo}</td>
            <td>${jxMoney(p.monto)}</td>
            <td>${badgeEstadoPago(p.estado)}</td>
        </tr>
    `).join('');
}

function mostrarAlertaPago(tipo, mensaje) {
    const alerta = document.getElementById('pago-alert');
    alerta.className = 'alert ' + (tipo === 'success' ? 'alert-success' : tipo === 'info' ? 'alert-info' : 'alert-danger');
    alerta.textContent = mensaje;
    alerta.classList.remove('d-none');
}

/* Callback global que exige CulqiJS v4 tras cerrar el checkout. */
async function culqi() {
    if (Culqi.token) {
        // Pago con tarjeta o Yape: en ambos casos llega un token; el mismo
        // endpoint de cargo sirve para los dos, Culqi lo distingue solo.
        const tokenId = Culqi.token.id;
        const esYape = tokenId.startsWith('ype_');
        mostrarAlertaPago('info', 'Procesando tu pago...');
        const r = await jxApi('PagoCulqiController', 'POST', {
            action: esYape ? 'cargoYape' : 'cargoTarjeta',
            idPlan: planSeleccionado,
            sourceId: tokenId
        });
        mostrarAlertaPago(r.success ? 'success' : 'error', r.message);
        if (r.success) { await cargarMiPlan(); await cargarMisPagos(); }

    } else if (Culqi.order) {
        // PagoEfectivo: el checkout de Culqi ya creó la orden (CIP) por su cuenta;
        // aquí solo la asociamos a este pago para poder confirmarla luego.
        const r = await jxApi('PagoCulqiController', 'POST', {
            action: 'registrarOrdenPagoEfectivo',
            idPlan: planSeleccionado,
            culqiOrderId: Culqi.order.id
        });
        mostrarAlertaPago(r.success ? 'success' : 'error',
            r.message || 'Anota tu código de pago (te lo mostró Culqi). Tu plan se activará apenas se confirme.');
        if (r.success) await cargarMisPagos();

    } else if (Culqi.error) {
        console.error('Error de Culqi:', Culqi.error);
        mostrarAlertaPago('error', Culqi.error.user_message || 'No se pudo procesar el pago.');
    }
}

function inicializarBotonPago() {
    document.getElementById('btn-pagar').addEventListener('click', () => {
        const select = document.getElementById('pago-plan');
        const opcion = select.selectedOptions[0];
        if (!opcion || !opcion.value) {
            mostrarAlertaPago('error', 'Selecciona un plan primero.');
            return;
        }
        planSeleccionado = opcion.value;
        const precio = parseFloat(opcion.dataset.precio);
        const nombrePlan = opcion.dataset.nombre;

        if (!CULQI_PUBLIC_KEY || CULQI_PUBLIC_KEY.includes('REEMPLAZA')) {
            mostrarAlertaPago('error', 'Los pagos en línea aún no están configurados (falta la llave pública de Culqi).');
            return;
        }

        Culqi.publicKey = CULQI_PUBLIC_KEY;
        Culqi.settings({
            title: 'JumpxFitness',
            currency: 'PEN',
            description: 'Plan ' + nombrePlan,
            amount: Math.round(precio * 100) // Culqi trabaja en céntimos
        });
        Culqi.options({
            lang: 'auto',
            installments: false,
            paymentMethods: { tarjeta: true, yape: true, bancaMovil: true, agente: true, billetera: false, cuotealo: false }
        });
        Culqi.open();
    });
}

async function jxPageInit() {
    if (!jxRequireLogin(['CLIENTE'])) return;
    inicializarBotonPago();
    await Promise.all([cargarMiPlan(), cargarPlanesActivos(), cargarMisPagos()]);
}
