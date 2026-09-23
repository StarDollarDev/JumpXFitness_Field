const $ = (sel) => document.querySelector(sel);

function tipoReclamoSeleccionado() {
    const marcado = document.querySelector('input[name="rc-tipo-reclamo"]:checked');
    return marcado ? marcado.value : '';
}

async function enviarReclamo(event) {
    event.preventDefault();
    const form = $('#reclamo-form');

    // Bootstrap valida los campos con "required"; el radio de tipo se valida aparte.
    const tipoOk = tipoReclamoSeleccionado() !== '';
    form.classList.add('was-validated');
    if (!form.checkValidity() || !tipoOk) {
        if (!tipoOk) {
            document.querySelectorAll('input[name="rc-tipo-reclamo"]').forEach(r => r.classList.add('is-invalid'));
        }
        return;
    }

    const button = $('#rc-submit');
    const alertBox = $('#reclamo-alert');
    button.disabled = true;
    const textoOriginal = button.innerHTML;
    button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Enviando...';
    alertBox.className = 'alert d-none';

    const payload = {
        nombreCompleto: $('#rc-nombre').value.trim(),
        tipoDoc: $('#rc-tipo-doc').value,
        numeroDoc: $('#rc-numero-doc').value.trim(),
        direccion: $('#rc-direccion').value.trim(),
        telefono: $('#rc-telefono').value.trim(),
        correo: $('#rc-correo').value.trim(),
        apoderado: $('#rc-apoderado').value.trim(),
        tipoBien: $('#rc-tipo-bien').value,
        descripcionBien: $('#rc-descripcion-bien').value.trim(),
        monto: $('#rc-monto').value,
        tipoReclamo: tipoReclamoSeleccionado(),
        detalle: $('#rc-detalle').value.trim(),
        pedido: $('#rc-pedido').value.trim(),
        aceptaTerminos: $('#rc-terminos').checked
    };

    try {
        const response = await fetch('ReclamoController', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();
        if (!response.ok || !result.success) throw new Error(result.message || 'No se pudo registrar tu reclamo.');

        alertBox.className = 'alert alert-success';
        alertBox.textContent = result.message;
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'start' });
        form.reset();
        form.classList.remove('was-validated');
        document.querySelectorAll('input[name="rc-tipo-reclamo"]').forEach(r => r.classList.remove('is-invalid'));
    } catch (error) {
        alertBox.className = 'alert alert-danger';
        alertBox.textContent = error.message || 'Ocurrió un error. Inténtalo nuevamente.';
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } finally {
        button.disabled = false;
        button.innerHTML = textoOriginal;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    $('#reclamo-form').addEventListener('submit', enviarReclamo);
    document.querySelectorAll('input[name="rc-tipo-reclamo"]').forEach(r => {
        r.addEventListener('change', () => {
            document.querySelectorAll('input[name="rc-tipo-reclamo"]').forEach(x => x.classList.remove('is-invalid'));
        });
    });
});
