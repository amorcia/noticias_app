/**
 * app.js
 * Lógica compartida para toda la aplicación (Iconos, Modales, Votaciones Generales)
 */

document.addEventListener('DOMContentLoaded', () => {
    // Inicializar iconos Lucide
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
});

/* =========================================
   Lógica de Votación (Noticias)
   ========================================= */
async function toggleLike(id) {
    if (!verifyLogin()) return;
    try {
        const res = await fetch(`${getContextPath()}interacciones/noticia/${id}/votar?tipo=LIKE`, { method: 'POST' });
        if (res.ok) {
            // Recargar o actualizar UI dinámicamente
            // Para simplicidad en esta versión refactorizada, mantenemos reload 
            // a menos que se implemente actualización parcial
            location.reload();
        } else {
            showSuccess('Error', 'No se pudo registrar tu voto.', 'error');
        }
    } catch (e) { console.error(e); }
}

async function toggleDislike(id) {
    if (!verifyLogin()) return;
    try {
        const res = await fetch(`${getContextPath()}interacciones/noticia/${id}/votar?tipo=DISLIKE`, { method: 'POST' });
        if (res.ok) {
            location.reload();
        } else {
            showSuccess('Error', 'No se pudo registrar tu voto.', 'error');
        }
    } catch (e) { console.error(e); }
}

/* =========================================
   Lógica de Modales (Reporte, Borrado, Éxito)
   ========================================= */

// --- Modal de Reporte ---
const reportModal = document.getElementById('reportModal');

function openReportModal(noticiaId, comentarioId = null) {
    if (!verifyLogin()) return;

    // Reset form
    document.getElementById('reportForm').reset();
    document.getElementById('customReasonGroup').style.display = 'none';

    // Set IDs
    if (document.getElementById('reportNoticiaId')) document.getElementById('reportNoticiaId').value = noticiaId || '';
    if (document.getElementById('reportComentarioId')) document.getElementById('reportComentarioId').value = comentarioId || '';

    // Title update (optional)
    const title = document.getElementById('reportModalTitle');
    if (title) title.innerText = comentarioId ? 'Denunciar Comentario' : 'Denunciar Noticia';

    reportModal.classList.add('open');
}

function closeReportModal() {
    if (reportModal) reportModal.classList.remove('open');
}

function checkReason() {
    const r = document.getElementById('reportReason').value;
    const customGroup = document.getElementById('customReasonGroup');
    if (customGroup) customGroup.style.display = (r === 'OTRO' ? 'block' : 'none');
}

async function submitReport() {
    const noticiaId = document.getElementById('reportNoticiaId')?.value;
    const comentarioId = document.getElementById('reportComentarioId')?.value;

    let motivo = document.getElementById('reportReason').value;
    if (motivo === 'OTRO') motivo = document.getElementById('customReason').value;

    const descripcion = document.getElementById('reportDescription').value;

    if (!motivo) {
        showSuccess('Error', 'Por favor selecciona un motivo.', 'error');
        return;
    }

    const payload = {
        usuarioId: getUserId(),
        motivo: motivo,
        descripcion: descripcion
    };

    if (noticiaId) payload.noticiaId = Number(noticiaId);
    if (comentarioId) payload.comentarioId = Number(comentarioId);

    try {
        const res = await fetch(`${getContextPath()}interacciones/denuncias`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            closeReportModal();
            showSuccess('Reporte Enviado', 'El equipo de moderación revisará tu reporte.');
        } else {
            showSuccess('Error', 'No se pudo enviar el reporte.', 'error');
        }
    } catch (e) { console.error(e); }
}

// --- Modal de Éxito/Error ---
const successModal = document.getElementById('successModal');

function showSuccess(title, msg, type = 'success') {
    if (!successModal) return alert(msg); // Fallback

    const titleEl = document.getElementById('successTitle');
    const msgEl = document.getElementById('successMessage');
    const iconContainer = document.getElementById('modalIconContainer');

    titleEl.innerText = title;
    msgEl.innerText = msg;

    if (type === 'error') {
        iconContainer.innerHTML = '<i data-lucide="x-circle" style="width: 64px; height: 64px; color: var(--danger);"></i>';
        titleEl.style.color = 'var(--danger)';
    } else {
        iconContainer.innerHTML = '<i data-lucide="check-circle" style="width: 64px; height: 64px; color: var(--success);"></i>';
        titleEl.style.color = 'var(--text-main)';
    }

    successModal.classList.add('open');
    if (typeof lucide !== 'undefined') lucide.createIcons();
}

function closeSuccessModal() {
    if (successModal) successModal.classList.remove('open');
}

/* =========================================
   Utilidades
   ========================================= */
function getContextPath() {
    // Definida globalmente en el layout o template
    return window.CONTEXT_PATH || '/';
}

function getUserId() {
    return window.USUARIO_ID || 0;
}

function verifyLogin() {
    if (!getUserId()) {
        location.href = `${getContextPath()}auth/login`;
        return false;
    }
    return true;
}
