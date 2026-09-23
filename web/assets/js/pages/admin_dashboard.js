async function jxPageInit() {
    if (!jxRequireLogin('ADMIN')) return;
    await cargarKpis();
}
async function cargarKpis() {
    const r = await jxApi('DashboardController', 'GET', { action: 'kpis' });
    if (!r.success) return;
    const d = r.data;
    document.getElementById('kpi-clientes').textContent = d.totalClientes ?? 0;
    document.getElementById('kpi-reservas').textContent = d.reservasHoy ?? 0;
    document.getElementById('kpi-sesiones').textContent = d.sesionesJumpingHoy ?? 0;
    document.getElementById('kpi-ventas').textContent = jxMoney(d.ventasHoy);
}
