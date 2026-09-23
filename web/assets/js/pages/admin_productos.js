let productosCache = [];
        let categoriasCache = [];
        const STOCK_MINIMO = 5;

        async function jxPageInit() {
            if (!jxRequireLogin('ADMIN')) return;
            document.getElementById('form-producto').addEventListener('submit', guardarProducto);
            await Promise.all([cargarCategorias(), cargarProductos()]);
        }

        // ---------- CATEGORIAS ----------
        async function cargarCategorias() {
            const r = await jxApi('CategoriaProductoController', 'GET', { action: 'listar' });
            categoriasCache = r.data || [];
            document.getElementById('pr_categoria').innerHTML =
                categoriasCache.map(c => `<option value="${c.id_categoria}">${c.nombre}</option>`).join('');
            document.getElementById('lista-categorias').innerHTML =
                categoriasCache.map(c => `<li class="list-group-item">${c.nombre}</li>`).join('')
                || '<li class="list-group-item text-muted">Sin categorías</li>';
        }

        function abrirModalCategoria() {
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalCategoria')).show();
        }

        async function guardarCategoria() {
            const nombre = document.getElementById('cat_nombre').value.trim();
            if (!nombre) { jxToast('error', 'Escribe el nombre de la categoría'); return; }
            const r = await jxApi('CategoriaProductoController', 'POST', { action: 'insertar', nombre });
            if (r.success) {
                document.getElementById('cat_nombre').value = '';
                jxToast('success', 'Categoría agregada');
                cargarCategorias();
            } else jxToast('error', r.message || 'No se pudo agregar');
        }

        // ---------- PRODUCTOS ----------
        function abrirModalProducto() {
            if (categoriasCache.length === 0) {
                jxToast('error', 'Primero crea al menos una categoría');
                return;
            }
            document.getElementById('form-producto').reset();
            document.getElementById('pr_id').value = '';
            document.getElementById('tituloModalProducto').textContent = 'Nuevo producto';
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalProducto')).show();
        }

        function editarProducto(id) {
            const p = productosCache.find(x => String(x.id_producto) === String(id));
            if (!p) return;
            document.getElementById('pr_id').value = p.id_producto;
            document.getElementById('pr_nombre').value = p.nombre;
            document.getElementById('pr_descripcion').value = p.descripcion || '';
            document.getElementById('pr_precio_compra').value = p.precioCompra;
            document.getElementById('pr_precio_venta').value = p.precioVenta;
            document.getElementById('pr_stock').value = p.stock;
            if (p.categoria) document.getElementById('pr_categoria').value = p.categoria.id_categoria;
            document.getElementById('tituloModalProducto').textContent = 'Editar producto';
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalProducto')).show();
        }

        async function guardarProducto(e) {
            e.preventDefault();
            const id = document.getElementById('pr_id').value;
            const payload = {
                nombre: document.getElementById('pr_nombre').value.trim(),
                descripcion: document.getElementById('pr_descripcion').value.trim(),
                precio_compra: document.getElementById('pr_precio_compra').value || 0,
                precio_venta: document.getElementById('pr_precio_venta').value,
                stock: document.getElementById('pr_stock').value || 0,
                id_categoria: document.getElementById('pr_categoria').value
            };
            const r = id
                ? await jxApi('ProductoController', 'POST', { action: 'actualizar', id, ...payload })
                : await jxApi('ProductoController', 'POST', { action: 'insertar', ...payload });

            if (r.success) {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('modalProducto')).hide();
                jxToast('success', id ? 'Producto actualizado' : 'Producto creado');
                cargarProductos();
            } else jxToast('error', r.message || 'No se pudo guardar');
        }

        async function eliminarProducto(id) {
            const conf = await Swal.fire({ title: '¿Eliminar este producto?', icon: 'warning', showCancelButton: true, confirmButtonText: 'Sí, eliminar' });
            if (!conf.isConfirmed) return;
            const r = await jxApi('ProductoController', 'POST', { action: 'eliminar', id });
            if (r.success) { jxToast('success', 'Producto eliminado'); cargarProductos(); }
            else jxToast('error', r.message || 'No se pudo eliminar');
        }

        // ---------- STOCK ----------
        function abrirModalStock(id) {
            const p = productosCache.find(x => String(x.id_producto) === String(id));
            if (!p) return;
            document.getElementById('stock_id').value = id;
            document.getElementById('stock_cantidad').value = 1;
            document.getElementById('stock_producto_nombre').textContent = `${p.nombre} (stock: ${p.stock})`;
            bootstrap.Modal.getOrCreateInstance(document.getElementById('modalStock')).show();
        }

        async function ajustarStock(accion) {
            const id = document.getElementById('stock_id').value;
            const cantidad = document.getElementById('stock_cantidad').value;
            const r = await jxApi('ProductoController', 'POST', { action: accion, id, cantidad });
            if (r.success) {
                bootstrap.Modal.getOrCreateInstance(document.getElementById('modalStock')).hide();
                jxToast('success', 'Stock actualizado');
                cargarProductos();
            } else jxToast('error', r.message || 'No se pudo ajustar el stock');
        }

        // ---------- TABLA ----------
        async function cargarProductos() {
            const r = await jxApi('ProductoController', 'GET', { action: 'listar' });
            productosCache = r.data || [];

            const bajos = productosCache.filter(p => p.stock <= STOCK_MINIMO);
            const alerta = document.getElementById('alerta-stock');
            if (bajos.length) {
                document.getElementById('alerta-stock-texto').textContent =
                    `${bajos.length} producto(s) con stock bajo: ` + bajos.map(p => `${p.nombre} (${p.stock})`).join(', ');
                alerta.classList.remove('d-none');
            } else alerta.classList.add('d-none');

            document.querySelector('#tabla-productos tbody').innerHTML = productosCache.map(p => `
                <tr>
                    <td>${p.nombre}<br><small class="text-muted">${p.descripcion || ''}</small></td>
                    <td>${p.categoria ? p.categoria.nombre : '—'}</td>
                    <td>${jxMoney(p.precioCompra)}</td>
                    <td>${jxMoney(p.precioVenta)}</td>
                    <td><span class="badge ${p.stock <= STOCK_MINIMO ? 'bg-danger' : 'bg-success'}">${p.stock}</span></td>
                    <td class="text-nowrap">
                        <button class="btn btn-sm btn-outline-success" title="Stock" onclick="abrirModalStock(${p.id_producto})"><i class="bi bi-box-arrow-in-down"></i></button>
                        <button class="btn btn-sm btn-outline-primary" title="Editar" onclick="editarProducto(${p.id_producto})"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger" title="Eliminar" onclick="eliminarProducto(${p.id_producto})"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>
            `).join('');

            $('#tabla-productos').DataTable({ destroy: true, language: { url: 'https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json' } });
        }
