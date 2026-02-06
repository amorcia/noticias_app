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

// --- Modal de Borrado (Unified) ---
const deleteModal = document.getElementById('deleteModal');

function openDeleteModal(id, titulo, esPropio) {
    if (!deleteModal) return;

    // Set hidden fields
    document.getElementById('deleteId').value = id;
    document.getElementById('deleteTitulo').value = titulo;
    document.getElementById('esPropio').value = esPropio;
    document.getElementById('tituloConfirmacionDisplay').innerText = titulo;

    // Update modal title for admins to include article title
    const modalTitle = document.getElementById('deleteModalTitle');
    if (!esPropio && modalTitle) {
        modalTitle.innerHTML = `Eliminar Noticia: <span style="color: var(--text-muted); font-weight: 500;">${titulo}</span>`;
    } else if (modalTitle) {
        modalTitle.textContent = 'Eliminar Noticia';
    }

    const adminForm = document.getElementById('adminDeleteForm');
    const ownerForm = document.getElementById('ownerDeleteForm');
    const motivoSelect = document.getElementById('deleteMotivoSelect');
    const descripcionTxt = document.getElementById('deleteDescripcion');
    const confirmInput = document.getElementById('confirmTituloInput');

    if (esPropio) {
        // Owner flow: title confirmation
        adminForm.style.display = 'none';
        ownerForm.style.display = 'block';
        motivoSelect.required = false;
        descripcionTxt.required = false;
        confirmInput.required = true;
        confirmInput.value = '';
    } else {
        // Admin flow: reason + description
        ownerForm.style.display = 'none';
        adminForm.style.display = 'block';
        motivoSelect.required = true;
        descripcionTxt.required = true;
        confirmInput.required = false;
        motivoSelect.value = '';
        descripcionTxt.value = '';
    }

    deleteModal.classList.add('open');
}

function closeDeleteModal() {
    if (deleteModal) deleteModal.classList.remove('open');
}

// Handle delete form submission (attach once on load)
document.addEventListener('DOMContentLoaded', () => {
    const deleteForm = document.getElementById('deleteForm');
    if (deleteForm) {
        deleteForm.addEventListener('submit', async function (event) {
            event.preventDefault();
            const formData = new FormData(event.target);
            const id = document.getElementById('deleteId').value;
            formData.append('id', id);

            const esPropio = document.getElementById('esPropio').value === 'true';
            const titulo = document.getElementById('deleteTitulo').value;

            // Validation for owner: title confirmation
            if (esPropio) {
                const confirmInput = document.getElementById('confirmTituloInput').value;
                if (confirmInput !== titulo) {
                    showSuccess('Error', 'El título no coincide. Por favor, escríbelo exactamente.', 'error');
                    return;
                }
            } else {
                // Validation for admin: reason + description
                const motivo = document.getElementById('deleteMotivoSelect').value;
                const descripcion = document.getElementById('deleteDescripcion').value;
                if (!motivo || !descripcion) {
                    showSuccess('Error', 'Debes completar motivo y descripción.', 'error');
                    return;
                }
            }

            try {
                const res = await fetch(`${getContextPath()}trabajador/noticias/borrar`, {
                    method: 'POST',
                    body: new URLSearchParams(formData)
                });

                if (res.ok) {
                    closeDeleteModal();
                    showSuccess('Eliminado', 'La noticia ha sido eliminada correctamente.');
                    setTimeout(() => location.reload(), 1500);
                } else {
                    const txt = await res.text();
                    showSuccess('Error', txt || 'No se pudo eliminar.', 'error');
                }
            } catch (e) {
                console.error(e);
                showSuccess('Error', 'Error de conexión.', 'error');
            }
        });
    }
});

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
    const userId = getUserId();
    if (!userId || userId === 0) {
        location.href = `${getContextPath()}auth/login`;
        return false;
    }
    return true;
}
