/**
 * Invite - Logica de la vista de invitacion
 */
const Invite = {

    init: function() {
        const input = document.getElementById('invite-code');
        if (input) {
            input.focus();

            // Auto-formatear el codigo (mayusculas)
            input.addEventListener('input', (e) => {
                e.target.value = e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
            });
        }
    },

    handleKeyPress: function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            this.join();
        }
    },

    join: async function() {
        const input = document.getElementById('invite-code');
        const code = input.value.trim();

        if (!code) {
            this.showError('Por favor ingresa un codigo de invitacion');
            return;
        }

        if (code.length < 6) {
            this.showError('El codigo debe tener al menos 6 caracteres');
            return;
        }

        // Limpiar mensajes
        this.showError('');
        this.showSuccess('');

        // Deshabilitar boton
        const btn = document.getElementById('join-btn');
        const originalText = btn.textContent;
        btn.disabled = true;
        btn.innerHTML = '<div class="spinner" style="width:20px;height:20px;border-width:2px;"></div> Uniendo...';

        try {
            const response = await Bridge.joinByInvite(code);

            this.showSuccess(response.message || 'Te has unido al chat exitosamente');

            // Navegar al chat despues de un momento
            setTimeout(() => {
                if (response.chatId && typeof loadPage === 'function') {
                    // Actualizar el chatId en sesion si es posible
                    Bridge.log('Joined chat: ' + response.chatId);
                    loadPage('chat');
                } else if (typeof goBack === 'function') {
                    goBack();
                }
            }, 1500);

        } catch (error) {
            console.error('Error joining:', error);
            this.showError(error.message || 'Error al unirse al chat');
            btn.disabled = false;
            btn.textContent = originalText;
        }
    },

    showError: function(message) {
        const el = document.getElementById('invite-error');
        el.textContent = message;
        el.classList.toggle('hidden', !message);

        // Ocultar success si se muestra error
        if (message) {
            document.getElementById('invite-success').classList.add('hidden');
        }
    },

    showSuccess: function(message) {
        const el = document.getElementById('invite-success');
        el.textContent = message;
        el.classList.toggle('hidden', !message);

        // Ocultar error si se muestra success
        if (message) {
            document.getElementById('invite-error').classList.add('hidden');
        }
    },

    showPreview: function(info) {
        const preview = document.getElementById('invite-preview');
        const avatarEl = document.getElementById('preview-avatar');
        const nameEl = document.getElementById('preview-name');
        const membersEl = document.getElementById('preview-members');

        if (info) {
            avatarEl.src = info.chatImage || '';
            nameEl.textContent = info.chatName || 'Chat';
            membersEl.textContent = info.memberCount ? `${info.memberCount} miembros` : '';
            preview.classList.remove('hidden');
        } else {
            preview.classList.add('hidden');
        }
    }
};

// Inicializar
Bridge.whenReady(() => Invite.init());
