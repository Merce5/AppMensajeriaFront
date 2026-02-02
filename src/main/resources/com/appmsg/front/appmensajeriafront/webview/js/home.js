/**
 * Home - Pagina principal con lista de chats
 */
const Home = {

    chats: [],

    init: function() {
        this.loadChats();
    },

    loadChats: function() {
        // Mostrar estado de carga
        this.showLoadingState();

        // Llamar al endpoint real
        this.refresh();
    },

    showLoadingState: function() {
        const container = document.getElementById('chats-list');
        container.innerHTML = `
            <div class="loading-state">
                <div class="loading-spinner"></div>
                <p>Cargando chats...</p>
            </div>
        `;
    },

    refresh: function() {
        try {
            Bridge.log('Requesting chats from Java');
            Bridge.getChats();
        } catch (error) {
            Bridge.log('Error requesting chats: ' + error);
            this.showEmptyState();
        }
    },

    renderChats: function(chats) {
        const container = document.getElementById('chats-list');
        const emptyState = document.getElementById('empty-state');

        if (!chats || chats.length === 0) {
            this.showEmptyState();
            return;
        }

        emptyState.classList.add('hidden');
        container.innerHTML = chats.map(chat => this.renderChatItem(chat)).join('');
    },

    renderChatItem: function(chat) {
        const name = chat.chatName || chat.name || 'Chat';
        const image = chat.chatImage || chat.image || '';
        const initials = this.getInitials(name);
        const time = chat.lastMessageTime ? Utils.formatRelative(chat.lastMessageTime) : '';
        const unreadBadge = chat.unreadCount > 0
            ? `<span class="chat-item-unread">${chat.unreadCount}</span>` : "";

        return `
            <div class="chat-item" onclick="Home.openChat('${chat.id}')">
                <div class="chat-item-avatar">
                    ${image
                        ? `<img src="${image}" alt="${name}">`
                        : initials}
                </div>
                <div class="chat-item-content">
                    <div class="chat-item-header">
                        <span class="chat-item-name">${Utils.escapeHtml(name)}</span>
                        <span class="chat-item-time">${time}</span>
                    </div>
                    <div class="chat-item-preview">
                        ${Utils.escapeHtml(chat.lastMessage || 'Sin mensajes')}
                        ${unreadBadge}
                    </div>
                </div>
            </div>
        `;
    },

    showEmptyState: function() {
        const container = document.getElementById('chats-list');
        const emptyState = document.getElementById('empty-state');

        container.innerHTML = '';
        emptyState.classList.remove('hidden');
    },

    openChat: function(chatId) {
        // Guardar chatId y navegar al chat
        Bridge.setChatId(chatId);
        Bridge.navigate('chat.html');
    },

    getInitials: function(name) {
        if (!name) return '?';
        return name
            .split(' ')
            .map(word => word.charAt(0))
            .join('')
            .substring(0, 2)
            .toUpperCase();
    },

    // Metodo para agregar un chat a la lista (util cuando te unes a uno nuevo)
    addChat: function(chat) {
        this.chats.unshift(chat);
        this.renderChats(this.chats);
    }
};

// Inicializar
Bridge.whenReady(() => {
    Bridge.log('Home page initialized');
    // Cargar ajustes y aplicar tema
    if (window.javaBridge && typeof javaBridge.loadSettings === "function") {
        javaBridge.loadSettings();
    }
    Home.init();
});

// Recibe los ajustes y aplica el tema
function onSettingsLoaded(dto) {
    if (dto && typeof dto.darkMode !== "undefined") {
        Utils.applyTheme(!!dto.darkMode);
    }
}
window.onChatsReceived = function (result) {
    const chats = (typeof result === "string") ? JSON.parse(result) : result;
    Home.renderChats(chats);
};
