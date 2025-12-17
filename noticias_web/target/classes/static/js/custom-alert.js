/**
 * Sistema de Alertas Personalizadas para TaskFlow
 * Reemplaza los alert() y confirm() nativos con modales estilizados.
 */

const CustomAlert = (function () {
    'use strict';

    let modal = null;
    let titleElement = null;
    let messageElement = null;
    let closeButton = null;
    let okButton = null;
    let cancelButton = null;
    let onConfirmCallback = null;

    function init() {
        if (!document.getElementById('custom-alert-modal')) {
            const modalHtml = `
                <div id="custom-alert-modal" class="modal-superposicion" style="z-index: 9999;">
                    <div class="modal-contenido" style="max-width: 400px; text-align: center;">
                        <button type="button" class="btn-cerrar-modal" id="custom-alert-close">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 18 18"/></svg>
                        </button>
                        <div style="margin-bottom: 1rem;" id="custom-alert-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#2C6BED" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
                        </div>
                        <h2 id="custom-alert-title" style="margin-bottom: 0.5rem;">Información</h2>
                        <p id="custom-alert-message" style="color: var(--color-texto-secundario); margin-bottom: 1.5rem;"></p>
                        <div style="display: flex; gap: 1rem; justify-content: center;">
                            <button type="button" class="btn btn-neutral" id="custom-alert-cancel" style="display: none; flex: 1;">Cancelar</button>
                            <button type="button" class="btn btn-primario" id="custom-alert-ok" style="flex: 1;">Aceptar</button>
                        </div>
                    </div>
                </div>
            `;
            document.body.insertAdjacentHTML('beforeend', modalHtml);
        }

        modal = document.getElementById('custom-alert-modal');
        titleElement = document.getElementById('custom-alert-title');
        messageElement = document.getElementById('custom-alert-message');
        closeButton = document.getElementById('custom-alert-close');
        okButton = document.getElementById('custom-alert-ok');
        cancelButton = document.getElementById('custom-alert-cancel');

        // Event Listeners
        closeButton.addEventListener('click', hide);

        okButton.addEventListener('click', function () {
            hide();
            if (onConfirmCallback) {
                onConfirmCallback();
            }
        });

        cancelButton.addEventListener('click', hide);

        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                hide();
            }
        });
    }

    function show(message, title = 'Información') {
        if (!modal) init();

        messageElement.textContent = message;
        titleElement.textContent = title;

        // Configuración para alerta simple
        cancelButton.style.display = 'none';
        okButton.textContent = 'Entendido';
        okButton.className = 'btn btn-primario';
        onConfirmCallback = null;

        modal.classList.add('visible');
        modal.style.display = 'flex';
    }

    function confirm(message, onConfirm, title = 'Confirmación') {
        if (!modal) init();

        messageElement.textContent = message;
        titleElement.textContent = title;

        // Configuración para confirmación
        cancelButton.style.display = 'block';
        okButton.textContent = 'Confirmar';
        okButton.className = 'btn btn-peligro'; // Color rojo para acciones destructivas
        onConfirmCallback = onConfirm;

        modal.classList.add('visible');
        modal.style.display = 'flex';
    }

    function hide() {
        if (modal) {
            modal.classList.remove('visible');
            setTimeout(() => {
                modal.style.display = 'none';
            }, 200);
        }
    }

    // Sobrescribir window.alert
    window.alert = function (message) {
        show(message);
    };

    return {
        show: show,
        confirm: confirm,
        hide: hide
    };
})();
