/**
 * comments.js
 * Gestión de comentarios para la vista de Detalle de Noticia
 */

// Referencias a modales específicos de comentarios
const deleteCommentModal = document.getElementById('deleteCommentModal');

/**
 * Renderiza un nodo de comentario y sus respuestas recursivamente
 */
function renderComment(c, depth = 0) {
    if (!c) return '';

    // Datos del autor
    const authorName = c.usuarioNombre || (c.autor ? c.autor.nombreCompleto : 'Anónimo');
    const authorId = c.usuarioId || (c.autor ? c.autor.id : null);
    const authorImagenUrl = c.usuarioImagenUrl || (c.autor ? c.autor.imagenUrl : null);
    const authorInitial = (authorName || '?').charAt(0).toUpperCase();

    // Permisos
    const currentUserId = getUserId();
    const currentUserRole = window.USUARIO_ROL || '';
    const isAuthor = (currentUserId && currentUserId === authorId);
    const canDelete = isAuthor || currentUserRole === 'ADMIN' || currentUserRole === 'OWNER';

    // Fecha
    const fechaStr = c.fecha ? new Date(c.fecha).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' }) : '';

    return `
        <div class="comment-node" id="comment-${c.id}" style="${depth > 0 ? '' : 'margin-left: 0;'}">
            <div class="comment-card">
                <div class="comment-meta">
                    <div class="comment-author-img" style="overflow: hidden; display: flex; align-items: center; justify-content: center;">
                        ${authorImagenUrl ? `<img src="${authorImagenUrl}" style="width: 100%; height: 100%; object-fit: cover;">` : authorInitial}
                    </div>
                    <div style="flex: 1;">
                        <div style="font-weight: 700; font-size: 1rem; color: var(--text-main);">${authorName}</div>
                        <div style="font-size: 0.8rem; color: var(--text-muted);">${fechaStr}</div>
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        ${canDelete ? `
                            <button onclick="confirmDeleteComment(${c.id})" class="btn-ghost" title="Eliminar" style="color: var(--danger); padding: 5px; background:none; border:none; cursor:pointer; opacity: 0.7; transition: opacity 0.2s;" onmouseover="this.style.opacity=1" onmouseout="this.style.opacity=0.7">
                                <i data-lucide="trash-2" style="width: 18px;"></i>
                            </button>
                        ` : ''}
                        <button onclick="openReportModal(null, ${c.id})" class="btn-ghost" title="Reportar" style="color: var(--text-muted); padding: 5px; background:none; border:none; cursor:pointer; opacity: 0.7; transition: opacity 0.2s;" onmouseover="this.style.opacity=1" onmouseout="this.style.opacity=0.7">
                            <i data-lucide="flag" style="width: 18px;"></i>
                        </button>
                    </div>
                </div>
                <div style="line-height: 1.6; font-size: 1.05rem; color: var(--text-main); margin: 0.5rem 0;">${c.contenido}</div>
                
                <div class="comment-actions">
                    <button onclick="voteComment(${c.id}, 'LIKE')" class="comment-action-btn ${c.votoUsuario === 'LIKE' ? 'active' : ''}" id="comment-like-${c.id}">
                        <i data-lucide="thumbs-up"></i> <span>${c.likes || 0}</span>
                    </button>
                    <button onclick="voteComment(${c.id}, 'DISLIKE')" class="comment-action-btn ${c.votoUsuario === 'DISLIKE' ? 'active' : ''}" id="comment-dislike-${c.id}">
                        <i data-lucide="thumbs-down"></i> <span>${c.dislikes || 0}</span>
                    </button>
                    <button onclick="toggleReplyForm(${c.id})" class="comment-action-btn">
                        <i data-lucide="reply"></i> Responder
                    </button>
                </div>

                <div id="reply-form-${c.id}" class="reply-form-container">
                    <textarea id="reply-text-${c.id}" class="form-input" rows="2" placeholder="Escribe tu respuesta..." 
                        style="width: 100%; margin: 0.5rem 0; padding: 0.75rem; border-radius: 0.5rem; border: 1px solid var(--border); background: var(--surface); color: var(--text-main);"></textarea>
                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                        <button onclick="toggleReplyForm(${c.id})" class="btn btn-ghost btn-sm">Cancelar</button>
                        <button onclick="postComment(${c.id})" class="btn btn-primary btn-sm">Responder</button>
                    </div>
                </div>
            </div>
            
            <div class="comment-replies" id="replies-${c.id}" style="${depth >= 3 ? 'border-left: none; padding-left: 0;' : ''}">
                ${c.respuestas && Array.isArray(c.respuestas) && c.respuestas.length > 0 ? c.respuestas.map(r => renderComment(r, depth + 1)).join('') : ''}
            </div>
        </div>
    `;
}

/**
 * Carga los comentarios via AJAX
 */
async function loadComments() {
    const list = document.getElementById('commentsList');
    const noticiaId = window.NOTICIA_ID; // Defined in HTML

    if (!noticiaId) return;

    try {
        let baseUrl = getContextPath();
        const url = `${baseUrl}interacciones/comentarios/noticia/${noticiaId}`;

        const response = await fetch(url);
        if (response.ok) {
            const comments = await response.json();
            list.innerHTML = '';

            if (!comments || comments.length === 0) {
                list.innerHTML = `
                    <div style="text-align: center; color: var(--text-muted); padding: 4rem 2rem; border: 2px dashed var(--border); border-radius: 1rem;">
                        <i data-lucide="message-square" style="width: 48px; height: 48px; margin-bottom: 1rem; opacity: 0.5;"></i>
                        <p>Aún no hay comentarios. Sé el primero en opinar.</p>
                    </div>
                `;
                lucide.createIcons();
                return;
            }

            let html = '';
            comments.forEach(c => {
                html += renderComment(c);
            });
            list.innerHTML = html;
            lucide.createIcons();
        } else {
            console.error("Failed to load comments:", response.status);
            list.innerHTML = '<p style="text-align:center; color: var(--danger);">Error al cargar los comentarios.</p>';
        }
    } catch (e) {
        console.error("Error in loadComments:", e);
        list.innerHTML = '<p style="text-align:center; color: var(--danger);">Error de conexión.</p>';
    }
}

/**
 * Publicar un comentario (o respuesta)
 */
async function postComment(parentId = null) {
    if (!verifyLogin()) return;

    const contentId = parentId ? `reply-text-${parentId}` : 'commentContent';
    const content = document.getElementById(contentId).value;

    if (!content || !content.trim()) {
        showSuccess('Error', 'El comentario no puede estar vacío', 'error');
        return;
    }

    try {
        const payload = {
            usuarioId: getUserId(),
            noticiaId: window.NOTICIA_ID,
            contenido: content,
            padreId: parentId
        };

        const res = await fetch(`${getContextPath()}interacciones/comentarios`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            document.getElementById(contentId).value = ''; // Clear input
            if (parentId) toggleReplyForm(parentId); // Close reply form
            loadComments(); // Reload list
            showSuccess('Comentario enviado', 'Tu comentario se ha publicado correctamente.');
        } else {
            showSuccess('Error', 'No se pudo publicar el comentario', 'error');
        }
    } catch (e) { console.error(e); }
}

function toggleReplyForm(id) {
    if (!verifyLogin()) return;
    const form = document.getElementById(`reply-form-${id}`);
    const text = document.getElementById(`reply-text-${id}`);

    if (!form) return;

    if (form.style.display === 'block') {
        form.style.display = 'none';
    } else {
        form.style.display = 'block';
        text.focus();
    }
}

async function voteComment(commentId, tipo) {
    if (!verifyLogin()) return;
    try {
        const res = await fetch(`${getContextPath()}interacciones/comentarios/${commentId}/votar?tipo=${tipo}`, { method: 'POST' });
        if (res.ok) {
            loadComments();
        }
    } catch (e) { console.error(e); }
}

// --- Gestión de Borrado de Comentarios ---

function confirmDeleteComment(id) {
    const hiddenInput = document.getElementById('deleteCommentId');
    const modal = document.getElementById('deleteCommentModal');
    if (hiddenInput && modal) {
        hiddenInput.value = id;
        modal.classList.add('open');
    }
}

function closeDeleteCommentModal() {
    const modal = document.getElementById('deleteCommentModal');
    if (modal) modal.classList.remove('open');
}

async function executeDeleteComment() {
    const id = document.getElementById('deleteCommentId').value;
    if (!id) return;

    try {
        const res = await fetch(`${getContextPath()}interacciones/comentarios/${id}`, { method: 'DELETE' });
        if (res.ok) {
            closeDeleteCommentModal();
            loadComments();
        } else {
            showSuccess('Error', 'No se pudo eliminar el comentario', 'error');
        }
    } catch (e) {
        console.error(e);
        showSuccess('Error', 'Error de conexión', 'error');
    }
}

// Cargar comentarios al inicio si estamos en la vista correcta
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('commentsList')) {
        loadComments();
    }
});
