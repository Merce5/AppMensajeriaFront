/**
 * Profile - Logica de la vista de perfil
 */
const Profile = {

    init: async function() {
        const params = Bridge.getInitParams();
        // Usar profileUserId si existe, sino el usuario actual
        const userId = params.profileUserId || Bridge.getUserId();

        if (!userId) {
            this.showError('No se especifico usuario');
            return;
        }

        try {
            const profile = await Bridge.loadProfile(userId);
            this.render(profile);
        } catch (error) {
            console.error('Error loading profile:', error);
            this.showError(error.message || 'No se pudo cargar el perfil');
        }
    },

    render: function(profile) {
        const content = document.getElementById('profile-content');

        const isOnline = profile.status === 'Online' || profile.status === 'En linea';
        const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent(`
            <svg viewBox="0 0 24 24" fill="none" stroke="%236b7280" stroke-width="1.5" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="8" r="4"/>
                <path d="M4 20c0-4 4-6 8-6s8 2 8 6"/>
            </svg>
        `);

        content.innerHTML = `
            <div class="profile-avatar-section">
                <img id="profile-picture"
                     class="profile-avatar"
                     src="${profile.picture || defaultAvatar}"
                     alt="${Utils.escapeHtml(profile.username || 'Usuario')}"
                     onerror="this.src='${defaultAvatar}'">
                <div class="profile-status-indicator ${isOnline ? 'online' : ''}"></div>
            </div>

            <div class="profile-info">
                <h2 class="profile-username">${Utils.escapeHtml(profile.username || 'Usuario')}</h2>
                <p class="profile-email">${Utils.escapeHtml(profile.email || '')}</p>
                ${profile.status ? `
                    <div class="profile-status">
                        <span class="status-badge ${isOnline ? 'online' : ''}"></span>
                        <span>${Utils.escapeHtml(profile.status)}</span>
                    </div>
                ` : ''}
            </div>

            <div class="profile-actions">
                <button class="btn btn-primary" onclick="Profile.startChat()">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
                    </svg>
                    Enviar mensaje
                </button>
            </div>
        `;
    },

    showError: function(message) {
        const content = document.getElementById('profile-content');
        content.innerHTML = `
            <div class="profile-error">
                <svg class="profile-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 8v4M12 16h.01"/>
                </svg>
                <p>${Utils.escapeHtml(message)}</p>
                <button class="btn btn-secondary" onclick="goBack()">Volver</button>
            </div>
        `;
    },

    startChat: function() {
        // Volver al chat o iniciar uno nuevo
        goBack();
    }
};

// Inicializar
Bridge.whenReady(() => Profile.init());
