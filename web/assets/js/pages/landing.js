let landingPlans = [];
let carouselIndex = 0;
let carouselVisible = 3;
let leadModal;

const $ = (selector) => document.querySelector(selector);

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function updateVisibleCount() {
    carouselVisible = window.innerWidth <= 991 ? 1 : 3;
    const max = Math.max(0, landingPlans.length - carouselVisible);
    carouselIndex = Math.min(carouselIndex, max);
    renderCarouselPosition();
}

function buildBenefits(plan) {
    const people = Number(plan.cantidadPersonas || 1);
    const days = Number(plan.diasVigencia || 1);
    const benefits = [
        `${people} persona${people === 1 ? '' : 's'} incluida${people === 1 ? '' : 's'}`,
        `Vigencia de ${days} día${days === 1 ? '' : 's'}`,
        'Acceso a sesiones de Jumping'
    ];
    if (days >= 30) benefits.push('Ideal para mantener una rutina constante');
    return benefits;
}

function renderPlans() {
    const status = $('#planes-status');
    const carousel = $('#planes-carousel');
    const track = $('#planes-track');

    if (!landingPlans.length) {
        status.textContent = 'Actualmente no hay planes publicados. Escríbenos para conocer las próximas opciones.';
        carousel.classList.add('d-none');
        return;
    }

    status.classList.add('d-none');
    carousel.classList.remove('d-none');
    track.innerHTML = landingPlans.map(plan => {
        const image = plan.imagen ? escapeHtml(plan.imagen) : 'assets/img/logo.png';
        const benefits = buildBenefits(plan).map(item => `<li><i class="bi bi-check-circle-fill"></i><span>${escapeHtml(item)}</span></li>`).join('');
        return `
            <article class="plan-card-public">
                <img class="plan-image" src="${image}" alt="Plan ${escapeHtml(plan.nombre)}" loading="lazy">
                <div class="plan-body">
                    <span class="plan-meta">${Number(plan.cantidadPersonas || 1)} persona${Number(plan.cantidadPersonas || 1) === 1 ? '' : 's'}</span>
                    <h3>${escapeHtml(plan.nombre)}</h3>
                    <div class="plan-price">S/ ${Number(plan.precio || 0).toFixed(2)} <small>/ plan</small></div>
                    <ul class="plan-benefits">${benefits}</ul>
                    <button type="button" class="btn btn-jx-primary rounded-pill w-100" data-plan-id="${Number(plan.id_plan)}">
                        Saber más <i class="bi bi-arrow-right ms-1"></i>
                    </button>
                </div>
            </article>`;
    }).join('');

    track.querySelectorAll('[data-plan-id]').forEach(button => {
        button.addEventListener('click', () => abrirLead(Number(button.dataset.planId)));
    });

    renderDots();
    updateVisibleCount();
}

function renderDots() {
    const dots = $('#planes-dots');
    const pages = Math.max(1, landingPlans.length - carouselVisible + 1);
    dots.innerHTML = Array.from({length: pages}, (_, index) =>
        `<button class="carousel-dot ${index === carouselIndex ? 'active' : ''}" type="button" aria-label="Ir al grupo ${index + 1}" data-carousel-index="${index}"></button>`
    ).join('');
    dots.querySelectorAll('[data-carousel-index]').forEach(dot => {
        dot.addEventListener('click', () => {
            carouselIndex = Number(dot.dataset.carouselIndex);
            renderCarouselPosition();
        });
    });
}

function renderCarouselPosition() {
    const track = $('#planes-track');
    if (!track || !track.children.length) return;
    const first = track.children[0];
    const gap = parseFloat(getComputedStyle(track).gap) || 20;
    const cardWidth = first.getBoundingClientRect().width;
    track.style.transform = `translateX(-${carouselIndex * (cardWidth + gap)}px)`;

    const max = Math.max(0, landingPlans.length - carouselVisible);
    $('#planes-prev').disabled = carouselIndex <= 0;
    $('#planes-next').disabled = carouselIndex >= max;

    document.querySelectorAll('.carousel-dot').forEach((dot, i) => dot.classList.toggle('active', i === carouselIndex));
}

function moveCarousel(direction) {
    const max = Math.max(0, landingPlans.length - carouselVisible);
    carouselIndex = Math.max(0, Math.min(max, carouselIndex + direction));
    renderCarouselPosition();
}

function abrirLead(idPlan = null) {
    const plan = landingPlans.find(p => Number(p.id_plan) === Number(idPlan));
    $('#lead-id-plan').value = plan ? plan.id_plan : '';
    $('#lead-plan-nombre').value = plan ? plan.nombre : '';
    $('#selected-plan').textContent = plan ? `Plan seleccionado: ${plan.nombre}` : 'Consulta general sobre JumpxFitness';
    $('#lead-feedback').className = 'alert d-none mb-0';
    $('#lead-feedback').textContent = '';
    $('#lead-form').classList.remove('was-validated');
    leadModal.show();
}

async function cargarPlanesLanding() {
    try {
        const response = await fetch('PlanJumpingController?action=listarActivos', { cache: 'no-store' });
        if (!response.ok) throw new Error('No se pudieron cargar los planes');
        const result = await response.json();
        if (!result.success) throw new Error(result.message || 'No se pudieron cargar los planes');
        landingPlans = Array.isArray(result.data) ? result.data : [];
        renderPlans();
    } catch (error) {
        console.error(error);
        $('#planes-status').textContent = 'No pudimos cargar los planes en este momento. Puedes dejarnos tus datos para recibir información.';
        $('#planes-carousel').classList.add('d-none');
    }
}

async function enviarLead(event) {
    event.preventDefault();
    const form = $('#lead-form');
    form.classList.add('was-validated');
    if (!form.checkValidity()) return;

    const button = $('#lead-submit');
    const feedback = $('#lead-feedback');
    button.disabled = true;
    button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Enviando...';
    feedback.className = 'alert d-none mb-0';

    const payload = {
        nombreCompleto: $('#lead-nombre').value.trim(),
        correo: $('#lead-correo').value.trim(),
        telefono: $('#lead-telefono').value.trim(),
        idPlan: $('#lead-id-plan').value ? Number($('#lead-id-plan').value) : null,
        planNombre: $('#lead-plan-nombre').value.trim()
    };

    try {
        const response = await fetch('LeadController', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();
        if (!response.ok || !result.success) throw new Error(result.message || 'No se pudo enviar el formulario');

        feedback.className = 'alert alert-success mb-0';
        feedback.textContent = result.message;
        form.reset();
        form.classList.remove('was-validated');
        $('#lead-id-plan').value = payload.idPlan ?? '';
        $('#lead-plan-nombre').value = payload.planNombre;
        setTimeout(() => leadModal.hide(), 1600);
    } catch (error) {
        feedback.className = 'alert alert-danger mb-0';
        feedback.textContent = error.message || 'Ocurrió un error. Inténtalo nuevamente.';
    } finally {
        button.disabled = false;
        button.innerHTML = 'Enviar solicitud';
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    leadModal = bootstrap.Modal.getOrCreateInstance($('#leadModal'));
    $('#lead-form').addEventListener('submit', enviarLead);
    $('#planes-prev').addEventListener('click', () => moveCarousel(-1));
    $('#planes-next').addEventListener('click', () => moveCarousel(1));
    $('#contact-main-cta').addEventListener('click', () => abrirLead());
    await cargarPlanesLanding();
    window.addEventListener('resize', () => {
        const oldVisible = carouselVisible;
        updateVisibleCount();
        if (oldVisible !== carouselVisible) renderDots();
    });
});
