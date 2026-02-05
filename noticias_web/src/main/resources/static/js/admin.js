/**
 * admin.js
 * Lógica para el Panel de Administración
 * Requiere: variables globales definidas en el HTML (ADMIN_STATS, CONTEXT_PATH)
 */

document.addEventListener('DOMContentLoaded', () => {
    // Inicializar iconos
    if (typeof lucide !== 'undefined') lucide.createIcons();

    // Inicializar Gráficas con manejo de errores
    if (window.ADMIN_STATS) {
        try {
            initCharts(window.ADMIN_STATS);
        } catch (e) {
            console.error("Error al iniciar gráficas:", e);
        }
    }

    // Attach global listeners that might be needed
    const deleteForm = document.getElementById('deleteForm');
    if (deleteForm) deleteForm.addEventListener('submit', handleDeletion);

    const vetoForm = document.getElementById('vetoForm');
    if (vetoForm) vetoForm.addEventListener('submit', handleVeto);

    const deleteUserForm = document.getElementById('deleteUserForm');
    if (deleteUserForm) deleteUserForm.addEventListener('submit', handleUserDeletion);
});

/* =========================================
   Tabs Navigation
   ========================================= */
function showTab(tabId, btn) {
    document.querySelectorAll('.tab-content').forEach(el => el.style.display = 'none');
    document.getElementById(tabId).style.display = 'block';

    document.querySelectorAll('.nav-btn').forEach(el => el.classList.remove('active'));
    if (btn) btn.classList.add('active');

    if (typeof lucide !== 'undefined') lucide.createIcons();
}

/* =========================================
   Charts Logic
   ========================================= */
function initCharts(stats) {
    const primaryColor = '#2563eb';
    const dangerColor = '#ef4444';

    const ctxBar = document.getElementById('barChart');
    if (ctxBar) {
        new Chart(ctxBar.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['Total Usuarios', 'Sanciones', 'Pendientes', 'Noticias'],
                datasets: [{
                    label: 'Registros',
                    data: [stats.totalUsuarios, stats.totalSanciones || 0, stats.sancionesPendientes, stats.totalNoticias || 0],
                    backgroundColor: [primaryColor, '#f59e0b', '#ef4444', '#10b981'],
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } }
            }
        });
    }

    const ctxPie = document.getElementById('pieChart');
    if (ctxPie) {
        const activos = (stats.totalUsuarios || 0) - (stats.usuariosVetados || 0);
        new Chart(ctxPie.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Activos', 'Vetados'],
                datasets: [{
                    data: [activos, stats.usuariosVetados],
                    backgroundColor: [primaryColor, dangerColor]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }
}

/* =========================================
   Modals & Actions
   ========================================= */

// --- Feedback Modal ---
function showFeedback(title, msg, type = 'success') {
    const modal = document.getElementById('feedbackModal');
    if (!modal) return alert(`${title}: ${msg}`);

    const titleEl = document.getElementById('feedbackTitle');
    const msgEl = document.getElementById('feedbackMessage');
    const iconContainer = document.getElementById('feedbackIconContainer');

    titleEl.innerText = title;
    msgEl.innerText = msg;

    if (type === 'error') {
        iconContainer.innerHTML = '<i data-lucide="x-circle" style="width: 64px; height: 64px; color: var(--danger);"></i>';
        titleEl.style.color = 'var(--danger)';
    } else {
        iconContainer.innerHTML = '<i data-lucide="check-circle" style="width: 64px; height: 64px; color: var(--success);"></i>';
        titleEl.style.color = 'var(--text-main)';
    }

    modal.classList.add('open');
    if (typeof lucide !== 'undefined') lucide.createIcons();
}

function closeFeedbackModal() {
    const modal = document.getElementById('feedbackModal');
    if (modal) modal.classList.remove('open');
}

// --- Delete News Logic ---
const deleteModal = document.getElementById('deleteModal');
let currentReportId = null;

function openDeleteModal(id, titulo, reportId = null) {
    document.getElementById('deleteId').value = id;
    document.getElementById('deleteTitulo').value = titulo;
    currentReportId = reportId;
    if (deleteModal) deleteModal.classList.add('open');
}

function closeDeleteModal() {
    if (deleteModal) deleteModal.classList.remove('open');
    currentReportId = null;
}

async function handleDeletion(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    try {
        let baseUrl = getContextPath();

        const res = await fetch(`${baseUrl}trabajador/noticias/borrar`, {
            method: 'POST',
            body: new URLSearchParams(formData)
        });

        if (res.ok) {
            if (currentReportId) {
                await fetch(`${baseUrl}admin/denuncias/${currentReportId}/resolver?estado=RESUELTA`, { method: 'POST' });
            }
            closeDeleteModal();
            showFeedback('Eliminado', 'La noticia ha sido borrada.', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showFeedback('Error', 'No se pudo eliminar la noticia.', 'error');
        }
    } catch (e) {
        console.error(e);
        showFeedback('Error', 'Error de conexión.', 'error');
    }
}

// --- Veto User Logic ---
const vetoModal = document.getElementById('vetoModal');

function openVetoModal(userId) {
    document.getElementById('vetoUserId').value = userId;
    if (vetoModal) vetoModal.classList.add('open');
}

function closeVetoModal() {
    if (vetoModal) vetoModal.classList.remove('open');
}

async function handleVeto(event) {
    event.preventDefault();
    const userId = document.getElementById('vetoUserId').value;
    const motivoSelect = document.getElementById('vetoMotivoSelect').value;
    const descripcion = document.getElementById('vetoDescripcion').value;
    const duracion = document.getElementById('vetoDuracion').value;

    if (!motivoSelect || (!descripcion && motivoSelect === 'OTRO')) {
        alert('Debes indicar un motivo y una descripción.');
        return;
    }

    const motivoFinal = motivoSelect + (descripcion ? " - " + descripcion : "");
    const params = new URLSearchParams();
    params.append('motivo', motivoFinal);
    params.append('duracion', duracion);

    try {
        let baseUrl = getContextPath();
        const res = await fetch(`${baseUrl}admin/usuarios/${userId}/vetar`, {
            method: 'POST',
            body: params
        });

        if (res.ok) {
            closeVetoModal();
            showFeedback('Usuario Vetado', 'El usuario ha sido vetado correctamente.', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showFeedback('Error', 'No se pudo vetar al usuario.', 'error');
        }
    } catch (e) {
        console.error(e);
        showFeedback('Error', 'Error de conexión.', 'error');
    }
}

// --- Veto Details & Unveto ---
const vetoDetailModal = document.getElementById('vetoDetailModal');

function openVetoDetailModal(id, motivoFull, hasta) {
    let reason = motivoFull;
    let desc = 'No especificada';

    if (motivoFull && motivoFull.includes(' - ')) {
        const parts = motivoFull.split(' - ');
        reason = parts[0];
        desc = parts.slice(1).join(' - ');
    }

    document.getElementById('detailMotivo').innerText = reason || 'Sin motivo';
    document.getElementById('detailDescripcion').innerText = desc;
    document.getElementById('detailHasta').innerText = hasta || 'Permanente';

    const btn = document.getElementById('desvetarBtn');
    btn.onclick = () => desvetarUsuario(id);

    if (vetoDetailModal) vetoDetailModal.classList.add('open');
    if (typeof lucide !== 'undefined') lucide.createIcons();
}

function closeVetoDetailModal() {
    if (vetoDetailModal) vetoDetailModal.classList.remove('open');
}

async function desvetarUsuario(id) {
    try {
        let baseUrl = getContextPath();
        const res = await fetch(`${baseUrl}admin/usuarios/${id}/desvetar`, { method: 'POST' });
        if (res.ok) {
            showFeedback('Usuario Activado', 'El usuario vuelve a tener acceso.', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showFeedback('Error', 'No se pudo activar al usuario.', 'error');
        }
    } catch (e) { console.error(e); }
}

// --- Delete User Logic ---
const deleteUserModal = document.getElementById('deleteUserModal');

function openDeleteUserModal(id, email) {
    document.getElementById('deleteUserId').value = id;
    document.getElementById('deleteUserEmailDisplay').textContent = email;
    if (deleteUserModal) deleteUserModal.classList.add('open');
}

function closeDeleteUserModal() {
    if (deleteUserModal) deleteUserModal.classList.remove('open');
}

function eliminarUsuario(id, email) {
    openDeleteUserModal(id, email);
}

async function handleUserDeletion(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const id = formData.get('id');
    const motivo = formData.get('motivo');
    const descripcion = formData.get('descripcion');

    try {
        let baseUrl = getContextPath();
        const params = new URLSearchParams();
        params.append('motivo', motivo);
        params.append('descripcion', descripcion);

        const res = await fetch(`${baseUrl}admin/usuarios/${id}/eliminar-con-justificacion`, {
            method: 'POST',
            body: params
        });

        if (res.ok) {
            closeDeleteUserModal();
            showFeedback('Usuario Eliminado', 'El usuario ha sido eliminado con justificación.', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            const error = await res.text();
            showFeedback('Error', error || 'No se pudo eliminar al usuario.', 'error');
        }
    } catch (e) {
        console.error(e);
        showFeedback('Error', 'Error de conexión.', 'error');
    }
}

// --- Roles & Sanctions ---
async function cambiarRol(id, rolId) {
    try {
        let baseUrl = getContextPath();
        const res = await fetch(`${baseUrl}admin/usuarios/${id}/rol?rolId=${rolId}`, { method: 'POST' });
        if (res.ok) {
            showFeedback('Rol Actualizado', 'El rol del usuario ha sido modificado.', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            const error = await res.text();
            showFeedback('Error', error || 'No se pudo cambiar el rol.', 'error');
        }
    } catch (e) { console.error(e); }
}

async function handleResolverSancion(event, id) {
    event.preventDefault();
    const formData = new FormData(event.target);
    try {
        let baseUrl = getContextPath();
        const res = await fetch(`${baseUrl}admin/sanciones/${id}/resolver`, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: new URLSearchParams(formData)
        });
        if (res.ok) {
            showFeedback('Sanción Resuelta', 'La sanción ha sido procesada correctamente.', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showFeedback('Error', 'No se pudo resolver la sanción.', 'error');
        }
    } catch (e) {
        console.error(e);
        showFeedback('Error', 'Error de conexión.', 'error');
    }
}

async function resolverDenuncia(id, estado) {
    try {
        let baseUrl = getContextPath();
        const res = await fetch(`${baseUrl}admin/denuncias/${id}/resolver?estado=${estado}`, { method: 'POST' });
        if (res.ok) {
            showFeedback('Reporte Actualizado', 'El reporte ha sido marcado como ' + estado.toLowerCase() + '.', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showFeedback('Error', 'No se pudo actualizar el reporte.', 'error');
        }
    } catch (e) { console.error(e); }
}

// --- Deleted News Details ---
const detalleEliminadaModal = document.getElementById('detalleEliminadaModal');

async function verDetalleEliminada(id) {
    if (detalleEliminadaModal) detalleEliminadaModal.classList.add('open');
    const contentDiv = document.getElementById('detalleEliminadaContent');

    try {
        let baseUrl = getContextPath();
        const res = await fetch(`${baseUrl}noticias/eliminadas/${id}`);

        if (!res.ok) throw new Error('No se pudo cargar el detalle');

        const noticia = await res.json();
        renderDeletedNewsDetail(contentDiv, noticia);

    } catch (error) {
        console.error('Error loading deleted news detail:', error);
        contentDiv.innerHTML = `
            <div style="text-align: center; padding: 3rem; color: var(--danger);">
                <i data-lucide="alert-circle" style="width: 48px; height: 48px; margin-bottom: 1rem;"></i>
                <p style="font-weight: 600; margin-bottom: 0.5rem;">Error al cargar el detalle</p>
                <p style="color: var(--text-muted); font-size: 0.9rem;">No se pudo obtener la información.</p>
            </div>
        `;
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }
}

function closeDetalleEliminadaModal() {
    if (detalleEliminadaModal) detalleEliminadaModal.classList.remove('open');
}

function renderDeletedNewsDetail(container, noticia) {
    container.innerHTML = `
        <div style="display: grid; gap: 2rem;">
            ${noticia.imagenUrl ? `
                <div style="width: 100%; border-radius: 1rem; overflow: hidden; border: 1px solid var(--border);">
                    <img src="${noticia.imagenUrl}" alt="${noticia.titulo}" style="width: 100%; height: auto; display: block; max-height: 400px; object-fit: cover;">
                </div>
            ` : ''}
            
            <div>
                <div style="margin-bottom: 0.5rem;">
                    <span class="badge" style="background: ${noticia.categoriaColor}; color: white;">
                        ${noticia.categoriaNombre}
                    </span>
                </div>
                <h2 style="font-size: 2rem; font-weight: 800; color: var(--text-main); margin-bottom: 0.5rem;">${noticia.titulo}</h2>
                ${noticia.subtitulo ? `<p style="font-size: 1.25rem; color: var(--text-muted); font-weight: 500;">${noticia.subtitulo}</p>` : ''}
            </div>
            
            <div style="display: flex; align-items: center; gap: 1rem; padding: 1rem; background: #f8fafc; border-radius: 0.75rem; border: 1px solid var(--border);">
                <i data-lucide="user" style="width: 24px; height: 24px; color: var(--primary);"></i>
                <div>
                    <div style="font-weight: 600; color: var(--text-main);">Autor Original</div>
                    <div style="color: var(--text-muted); font-size: 0.9rem;">${noticia.autorOriginalNombre}</div>
                </div>
                <div style="margin-left: auto; text-align: right;">
                    <div style="font-size: 0.85rem; color: var(--text-muted);">Publicado</div>
                    <div style="font-weight: 600;">${new Date(noticia.fechaPublicacion).toLocaleDateString()}</div>
                </div>
            </div>
            
            <div style="padding: 1.5rem; background: white; border-radius: 1rem; border: 1px solid var(--border);">
                <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--text-main);">
                    <i data-lucide="file-text" style="width: 20px; height: 20px; display: inline; vertical-align: middle;"></i> Contenido
                </h3>
                <div style="line-height: 1.8; color: var(--text-main); white-space: pre-wrap;">${noticia.contenido}</div>
            </div>
            
            <div style="padding: 1.5rem; background: #fef2f2; border-radius: 1rem; border: 2px solid var(--danger);">
                <h3 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem; color: var(--danger);">
                    <i data-lucide="alert-triangle" style="width: 20px; height: 20px; display: inline; vertical-align: middle;"></i> Información de Eliminación
                </h3>
                <div style="display: grid; gap: 1rem;">
                    <div style="display: grid; grid-template-columns: 150px 1fr; gap: 0.5rem;">
                        <div style="font-weight: 600; color: var(--text-main);">Eliminado por:</div>
                        <div style="color: var(--text-main);">${noticia.eliminadoPorNombre} <span class="badge badge-accent" style="font-size: 0.75rem;">${noticia.rolEliminador}</span></div>
                    </div>
                    <div style="display: grid; grid-template-columns: 150px 1fr; gap: 0.5rem;">
                        <div style="font-weight: 600; color: var(--text-main);">Motivo:</div>
                        <div><span class="badge badge-danger">${noticia.motivo}</span></div>
                    </div>
                </div>
            </div>
        </div>
    `;
    if (typeof lucide !== 'undefined') lucide.createIcons();
}

/** 
 * Utility to get context path 
 */
function getContextPath() {
    // Defined in HTML template
    let path = window.CONTEXT_PATH || '/';
    return path.endsWith('/') ? path : path + '/';
}
