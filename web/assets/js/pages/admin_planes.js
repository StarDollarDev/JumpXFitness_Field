let planesCache = [];

async function jxPageInit() {
    if (!jxRequireLogin('ADMIN')) return;
    await cargarTablaPlanes();
    document.getElementById('form-plan').addEventListener('submit', guardarPlan);
}

function previsualizarImagenPlan(input) {
    const preview = document.getElementById('plan_imagen_preview');
    if (input.files && input.files[0]) {
        preview.src = URL.createObjectURL(input.files[0]);
        preview.classList.remove('d-none');
    }
}

function abrirModalNuevo() {
    document.getElementById('form-plan').reset();
    document.getElementById('plan_id').value = '';
    document.getElementById('plan_imagen_preview').classList.add('d-none');
    document.getElementById('tituloModalPlan').textContent = 'Nuevo plan';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalPlan')).show();
}

function editarPlan(id) {
    const p = planesCache.find(x => String(x.id_plan) === String(id));
    if (!p) return;
    document.getElementById('form-plan').reset();
    document.getElementById('plan_id').value = p.id_plan;
    document.getElementById('plan_nombre').value = p.nombre;
    document.getElementById('plan_precio').value = p.precio;
    document.getElementById('plan_personas').value = p.cantidadPersonas;
    document.getElementById('plan_dias').value = p.diasVigencia;
    const preview = document.getElementById('plan_imagen_preview');
    if (p.imagen) { preview.src = p.imagen; preview.classList.remove('d-none'); }
    else preview.classList.add('d-none');
    document.getElementById('tituloModalPlan').textContent = 'Editar plan';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalPlan')).show();
}

async function guardarPlan(e) {
    e.preventDefault();
    const id = document.getElementById('plan_id').value;

    // Con imagen (archivo) usamos FormData en vez de jxApi, que solo envía texto.
    const formData = new FormData();
    formData.append('action', id ? 'actualizar' : 'insertar');
    if (id) formData.append('id', id);
    formData.append('nombre', document.getElementById('plan_nombre').value.trim());
    formData.append('precio', document.getElementById('plan_precio').value);
    formData.append('cantidad_personas', document.getElementById('plan_personas').value);
    formData.append('dias_vigencia', document.getElementById('plan_dias').value);
    const archivo = document.getElementById('plan_imagen').files[0];
    if (archivo) formData.append('imagen', archivo);

    const res = await fetch('PlanJumpingController', { method: 'POST', body: formData });
    const r = await res.json();

    if (r.success) {
        bootstrap.Modal.getOrCreateInstance(document.getElementById('modalPlan')).hide();
        jxToast('success', id ? 'Plan actualizado' : 'Plan creado');
        cargarTablaPlanes();
    } else {
        jxToast('error', r.message || 'No se pudo guardar el plan');
    }
}

async function togglePlan(id, activo) {
    const r = await jxApi('PlanJumpingController', 'POST', { action: 'cambiarEstado', id, activo: !activo });
    if (r.success) { jxToast('success', 'Estado actualizado'); cargarTablaPlanes(); }
    else jxToast('error', r.message || 'Error al cambiar estado');
}

async function eliminarPlan(id) {
    const conf = await Swal.fire({ title: '¿Eliminar este plan?', icon: 'warning', showCancelButton: true, confirmButtonText: 'Sí, eliminar' });
    if (!conf.isConfirmed) return;
    const r = await jxApi('PlanJumpingController', 'POST', { action: 'eliminar', id });
    if (r.success) { jxToast('success', 'Plan eliminado'); cargarTablaPlanes(); }
    else jxToast('error', r.message || 'No se pudo eliminar');
}

async function cargarTablaPlanes() {
    const r = await jxApi('PlanJumpingController', 'GET', { action: 'listar' });
    planesCache = r.data || [];
    const tbody = document.querySelector('#tabla-planes tbody');
    tbody.innerHTML = planesCache.map(p => `
        <tr>
            <td>${p.imagen ? `<img src="${p.imagen}" class="rounded" style="width:48px; height:48px; object-fit:cover;">` : '<span class="text-muted small">Sin imagen</span>'}</td>
            <td>${p.nombre}</td>
            <td>${jxMoney(p.precio)}</td>
            <td>${p.cantidadPersonas}</td>
            <td>${p.diasVigencia} día(s)</td>
            <td><span class="badge ${p.activo ? 'bg-success' : 'bg-secondary'}">${p.activo ? 'Activo' : 'Inactivo'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="editarPlan(${p.id_plan})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-secondary" onclick="togglePlan(${p.id_plan}, ${p.activo})"><i class="bi bi-power"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="eliminarPlan(${p.id_plan})"><i class="bi bi-trash"></i></button>
            </td>
        </tr>
    `).join('');
    $('#tabla-planes').DataTable({ destroy: true, language: { url: 'https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json' } });
}
