let productosVenta = [];
        let carrito = [];
        let vtIdCliente = null;
        let ventasCache = [];

        async function jxPageInit() {
            if (!jxRequireLogin('ADMIN')) return;
            document.getElementById('rep_fecha').value = new Date().toISOString().slice(0, 10);
            document.getElementById('rep_fecha').addEventListener('change', cargarReporteDiario);
            await Promise.all([cargarProductosVenta(), cargarReporteDiario(), cargarGraficoSemana()]);
        }

        // ---------- NUEVA VENTA ----------
        async function cargarProductosVenta() {
            const r = await jxApi('ProductoController', 'GET', { action: 'listarConStock' });
            productosVenta = r.data || [];
            document.getElementById('vt_producto').innerHTML = productosVenta.map(p =>
                `<option value="${p.id_producto}">${p.nombre} — ${jxMoney(p.precioVenta)} (stock ${p.stock})</option>`
            ).join('') || '<option value="">Sin productos con stock</option>';
        }

        function abrirModalVenta() {
            carrito = [];
            vtIdCliente = null;
            document.getElementById('vt_doc_num').value = '';
            document.getElementById('vt_cliente_info').classList.add('d-none');
            renderCarrito();
            cargarProductosVenta();
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalVenta')).show();
        }

        function agregarAlCarrito() {
            const id = parseInt(document.getElementById('vt_producto').value);
            const cantidad = parseInt(document.getElementById('vt_cantidad').value);
            const prod = productosVenta.find(p => p.id_producto === id);
            if (!prod || !cantidad || cantidad < 1) return;

            const yaEnCarrito = carrito.find(i => i.idProducto === id);
            const totalPedido = (yaEnCarrito ? yaEnCarrito.cantidad : 0) + cantidad;
            if (totalPedido > prod.stock) {
                jxToast('error', `Solo hay ${prod.stock} unidades de ${prod.nombre}`);
                return;
            }

            if (yaEnCarrito) yaEnCarrito.cantidad = totalPedido;
            else carrito.push({ idProducto: id, nombre: prod.nombre, precio: prod.precioVenta, cantidad });
            renderCarrito();
        }

        function quitarDelCarrito(idx) {
            carrito.splice(idx, 1);
            renderCarrito();
        }

        function renderCarrito() {
            const tbody = document.getElementById('carrito-body');
            if (carrito.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Carrito vacío</td></tr>';
                document.getElementById('carrito-total').textContent = jxMoney(0);
                return;
            }
            tbody.innerHTML = carrito.map((i, idx) => `
                <tr>
                    <td>${i.nombre}</td><td>${i.cantidad}</td><td>${jxMoney(i.precio)}</td>
                    <td>${jxMoney(i.precio * i.cantidad)}</td>
                    <td><button class="btn btn-sm btn-outline-danger" onclick="quitarDelCarrito(${idx})"><i class="bi bi-x"></i></button></td>
                </tr>`).join('');
            const total = carrito.reduce((s, i) => s + i.precio * i.cantidad, 0);
            document.getElementById('carrito-total').textContent = jxMoney(total);
        }

        async function buscarClienteVenta() {
            const documento = document.getElementById('vt_doc_tipo').value;
            const numeroDoc = document.getElementById('vt_doc_num').value.trim();
            if (!numeroDoc) return;
            const r = await jxApi('ClienteController', 'GET', { action: 'buscarDocumento', documento, numeroDoc });
            const info = document.getElementById('vt_cliente_info');
            if (r.success && r.data) {
                vtIdCliente = r.data.id_cliente;
                info.textContent = 'Cliente: ' + r.data.persona.nombre + ' ' + r.data.persona.apellido;
                info.classList.remove('d-none');
            } else {
                vtIdCliente = null;
                info.classList.add('d-none');
                jxToast('info', 'Sin cliente: se registrará como venta anónima');
            }
        }

        async function registrarVenta() {
            if (carrito.length === 0) { jxToast('error', 'Agrega al menos un producto'); return; }

            const payload = {
                action: 'insertar',
                metodoPago: document.getElementById('vt_metodo').value,
                productos: JSON.stringify(carrito.map(i => ({ idProducto: i.idProducto, cantidad: i.cantidad, precio: i.precio })))
            };
            if (vtIdCliente) payload.idCliente = vtIdCliente;

            const r = await jxApi('VentaController', 'POST', payload);
            if (r.success) {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('modalVenta')).hide();
                jxToast('success', 'Venta registrada');
                cargarReporteDiario();
                cargarGraficoSemana();
            } else jxToast('error', r.message || 'No se pudo registrar la venta');
        }

        // ---------- REPORTE DIARIO ----------
        async function cargarReporteDiario() {
            const fecha = document.getElementById('rep_fecha').value;
            const r = await jxApi('VentaController', 'GET', { action: 'listarPorFecha', fecha });
            ventasCache = r.data || [];

            const total = ventasCache.reduce((s, v) => s + v.total, 0);
            const efectivo = ventasCache.filter(v => v.metodoPago === 'EFECTIVO').reduce((s, v) => s + v.total, 0);
            const yape = ventasCache.filter(v => v.metodoPago === 'YAPE').reduce((s, v) => s + v.total, 0);

            document.getElementById('rep-cantidad').textContent = ventasCache.length;
            document.getElementById('rep-total').textContent = jxMoney(total);
            document.getElementById('rep-efectivo').textContent = jxMoney(efectivo);
            document.getElementById('rep-yape').textContent = jxMoney(yape);

            document.querySelector('#tabla-ventas tbody').innerHTML = ventasCache.map(v => `
                <tr>
                    <td>${v.id_venta}</td>
                    <td>${v.cliente && v.cliente.persona ? v.cliente.persona.nombre + ' ' + v.cliente.persona.apellido : '<span class="text-muted">Sin cliente</span>'}</td>
                    <td><span class="badge ${v.metodoPago === 'YAPE' ? 'bg-primary' : 'bg-success'}">${v.metodoPago}</span></td>
                    <td>${jxMoney(v.total)}</td>
                    <td><button class="btn btn-sm btn-outline-primary" onclick="verDetalle(${v.id_venta})"><i class="bi bi-eye"></i></button></td>
                </tr>`).join('');

            $('#tabla-ventas').DataTable({ destroy: true, order: [[0, 'desc']], language: { url: 'https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json' } });
        }

        async function verDetalle(idVenta) {
            const r = await jxApi('VentaController', 'GET', { action: 'buscar', id: idVenta });
            const cont = document.getElementById('detalle-body');
            if (!r.success || !r.data) { cont.innerHTML = '<p class="text-muted">No se pudo cargar el detalle.</p>'; return; }
            const v = r.data;
            const items = v.detalles || [];
            cont.innerHTML = `
                <p class="mb-1"><strong>Venta #${v.id_venta}</strong> — ${v.metodoPago}</p>
                <table class="table table-sm">
                    <thead><tr><th>Producto</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead>
                    <tbody>${items.map(d => `
                        <tr>
                            <td>${d.producto ? d.producto.nombre : '—'}</td>
                            <td>${d.cantidad}</td><td>${jxMoney(d.precio)}</td><td>${jxMoney(d.subtotal)}</td>
                        </tr>`).join('') || '<tr><td colspan="4" class="text-muted">Sin detalle</td></tr>'}
                    </tbody>
                </table>
                <p class="text-end fw-bold mb-0">Total: ${jxMoney(v.total)}</p>`;
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDetalle')).show();
        }

        // ---------- GRAFICO SEMANAL ----------
        async function cargarGraficoSemana() {
            const r = await jxApi('DashboardController', 'GET', { action: 'ventasSemana' });
            const ventas = r.data || [];

            // Arma los últimos 7 días (incluido hoy) y suma el total vendido en cada uno,
            // en vez de dibujar una barra por cada venta individual.
            const dias = [];
            for (let i = 6; i >= 0; i--) {
                const d = new Date();
                d.setDate(d.getDate() - i);
                dias.push(d.toISOString().slice(0, 10));
            }
            const totalesPorDia = Object.fromEntries(dias.map(f => [f, 0]));
            ventas.forEach(v => {
                const fecha = (v.fecha || '').slice(0, 10);
                if (fecha in totalesPorDia) totalesPorDia[fecha] += (v.total || 0);
            });

            const labels = dias.map(f => new Date(f + 'T00:00:00').toLocaleDateString('es-PE', { day: '2-digit', month: 'short' }));
            const data = dias.map(f => totalesPorDia[f]);

            if (!window.Chart) {
                await new Promise(res => {
                    const s = document.createElement('script');
                    s.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js';
                    s.onload = res; document.body.appendChild(s);
                });
            }
            const ctx = document.getElementById('grafico-semana');
            if (window.graficoSemana) window.graficoSemana.destroy();
            window.graficoSemana = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels,
                    datasets: [{ label: 'Ingresos (S/)', data, backgroundColor: '#F5A623' }]
                },
                options: { responsive: true, plugins: { legend: { display: false } } }
            });
        }
