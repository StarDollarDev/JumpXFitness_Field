async function jxPageInit() {
            if (!jxRequireLogin()) return;
            await cargarTablaAuditoria();
        }

        function badgeAccion(a) {
            const colores = { INSERT: 'bg-success', UPDATE: 'bg-warning text-dark', DELETE: 'bg-danger',
                LOGIN: 'bg-info', LOGOUT: 'bg-secondary', LOGIN_FALLIDO: 'bg-dark' };
            return `<span class="badge ${colores[a] || 'bg-secondary'}">${a}</span>`;
        }

        async function cargarTablaAuditoria() {
            const tabla = document.getElementById('f-tabla').value;
            const accion = document.getElementById('f-accion').value;

            let action = 'listar';
            const params = {};
            if (tabla) { action = 'listarPorTabla'; params.tabla = tabla; }
            else if (accion) { action = 'listarPorAccion'; params.accion = accion; }

            const r = await jxApi('AuditoriaController', 'GET', { action, ...params });
            let data = r.data || [];
            if (tabla && accion) data = data.filter(a => a.accion === accion);

            const tbody = document.querySelector('#tabla-auditoria tbody');
            tbody.innerHTML = data.map(a => `
                <tr>
                    <td>${new Date(a.fechaHora).toLocaleString('es-PE')}</td>
                    <td>${a.usuarioNombre || '-'}</td>
                    <td>${badgeAccion(a.accion)}</td>
                    <td>${a.tablaAfectada}</td>
                    <td>${a.idRegistroAfectado || '-'}</td>
                    <td>${a.detalle || '-'}</td>
                    <td>${a.ipOrigen || '-'}</td>
                </tr>
            `).join('');
            $('#tabla-auditoria').DataTable({ destroy: true, order: [[0, 'desc']], language: { url: 'https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json' } });
        }
