async function jxPageInit() {
            if (!jxRequireLogin('ADMIN')) return;
            document.getElementById('filtro_fecha').value = new Date().toISOString().slice(0, 10);
            document.getElementById('filtro_fecha').addEventListener('change', cargarTabla);
            document.getElementById('form-registro').addEventListener('submit', guardarRegistro);
            document.getElementById('rg_fecha').addEventListener('change', cargarHorariosSelect);
            await cargarTabla();
        }

        let rgIdCliente = null;

        function abrirModalRegistro() {
            document.getElementById('form-registro').reset();
            rgIdCliente = null;
            document.getElementById('rg_cliente_info').classList.add('d-none');
            document.getElementById('rg_cliente_nuevo').classList.add('d-none');
            document.getElementById('rg_fecha').value = document.getElementById('filtro_fecha').value;
            cargarPlanesSelect();
            cargarHorariosSelect();
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalRegistro')).show();
        }

        async function buscarClienteRegistro() {
            const documento = document.getElementById('rg_doc_tipo').value;
            const numeroDoc = document.getElementById('rg_doc_num').value.trim();
            if (!numeroDoc) { jxToast('error', 'Ingresa el número de documento'); return; }

            const r = await jxApi('ClienteController', 'GET', { action: 'buscarDocumento', documento, numeroDoc });
            const info = document.getElementById('rg_cliente_info');
            const nuevo = document.getElementById('rg_cliente_nuevo');

            if (r.success && r.data) {
                rgIdCliente = r.data.id_cliente;
                info.textContent = 'Cliente: ' + r.data.persona.nombre + ' ' + r.data.persona.apellido;
                info.classList.remove('d-none');
                nuevo.classList.add('d-none');
            } else {
                rgIdCliente = null;
                info.classList.add('d-none');
                nuevo.classList.remove('d-none');
            }
        }

        async function cargarPlanesSelect() {
            const r = await jxApi('PlanJumpingController', 'GET', { action: 'listarActivos' });
            const sel = document.getElementById('rg_plan');
            sel.innerHTML = (r.data || []).map(p =>
                `<option value="${p.id_plan}" data-precio="${p.precio}">${p.nombre} — ${jxMoney(p.precio)}</option>`).join('');
            sincronizarMonto();
            sel.onchange = sincronizarMonto;
        }

        function sincronizarMonto() {
            const opt = document.getElementById('rg_plan').selectedOptions[0];
            if (opt) document.getElementById('rg_monto').value = opt.dataset.precio;
        }

        async function cargarHorariosSelect() {
            const fecha = document.getElementById('rg_fecha').value;
            const r = await jxApi('RegistroJumpingController', 'GET', { action: 'horariosDisponibles', fecha });
            const sel = document.getElementById('rg_horario');
            sel.innerHTML = (r.data && r.data.length)
                ? r.data.map(h => `<option value="${h.id_horario}">${h.hora_inicio} - ${h.hora_fin}</option>`).join('')
                : '<option value="">Sin horarios disponibles</option>';
        }

        async function guardarRegistro(e) {
            e.preventDefault();
            const idHorario = document.getElementById('rg_horario').value;
            if (!idHorario) { jxToast('error', 'No hay horario disponible'); return; }

            const payload = {
                action: 'insertar',
                id_plan: document.getElementById('rg_plan').value,
                id_horario: idHorario,
                metodo_pago: document.getElementById('rg_metodo').value,
                fecha: document.getElementById('rg_fecha').value,
                monto: document.getElementById('rg_monto').value
            };

            if (rgIdCliente) {
                payload.id_cliente = rgIdCliente;
            } else {
                const nombre = document.getElementById('rg_nombre').value.trim();
                const apellido = document.getElementById('rg_apellido').value.trim();
                const numeroDoc = document.getElementById('rg_doc_num').value.trim();
                if (!nombre || !apellido || !numeroDoc) {
                    jxToast('error', 'Busca un cliente o completa nombre, apellido y documento');
                    return;
                }
                payload.nombre = nombre;
                payload.apellido = apellido;
                payload.documento = document.getElementById('rg_doc_tipo').value;
                payload.numeroDoc = numeroDoc;
                payload.telefono = document.getElementById('rg_telefono').value.trim();
            }

            const r = await jxApi('RegistroJumpingController', 'POST', payload);
            if (r.success) {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('modalRegistro')).hide();
                jxToast('success', 'Sesión registrada');
                cargarTabla();
            } else {
                jxToast('error', r.message || 'No se pudo registrar');
            }
        }


        function nombreCliente(reg) {
            if (!reg.cliente || !reg.cliente.persona) return '—';
            return `${reg.cliente.persona.nombre} ${reg.cliente.persona.apellido}`;
        }

        async function eliminarRegistro(id) {
            const conf = await Swal.fire({ title: '¿Eliminar esta sesión?', icon: 'warning', showCancelButton: true, confirmButtonText: 'Sí, eliminar' });
            if (!conf.isConfirmed) return;
            const r = await jxApi('RegistroJumpingController', 'POST', { action: 'eliminar', id });
            if (r.success) { jxToast('success', 'Sesión eliminada'); cargarTabla(); }
            else jxToast('error', r.message || 'No se pudo eliminar');
        }

        async function cargarTabla() {
            const fecha = document.getElementById('filtro_fecha').value;
            const r = await jxApi('RegistroJumpingController', 'GET', { action: 'listarPorFecha', fecha });
            const tbody = document.querySelector('#tabla-registros tbody');
            const data = r.data || [];
            tbody.innerHTML = data.map(reg => `
                <tr>
                    <td>${nombreCliente(reg)}</td>
                    <td>${reg.plan ? reg.plan.nombre : '—'}</td>
                    <td>${reg.horario ? reg.horario.hora_inicio + ' - ' + reg.horario.hora_fin : '—'}</td>
                    <td>${reg.metodoPago}</td>
                    <td>${jxMoney(reg.monto)}</td>
                    <td><button class="btn btn-sm btn-outline-danger" onclick="eliminarRegistro(${reg.id_registro})"><i class="bi bi-trash"></i></button></td>
                </tr>
            `).join('');
            $('#tabla-registros').DataTable({ destroy: true, language: { url: 'https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json' } });
        }
