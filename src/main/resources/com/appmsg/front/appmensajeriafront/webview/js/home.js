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

    refresh: async function() {
        try {
            const userId = await javaBridge.getUserId();
            javaBridge.log('Fetching chats for user: ' + userId);
            const BASE_URL = 'http://localhost:8080/APPMensajeriaUEM_war_exploded//api';
            const response = await fetch(`${BASE_URL}/chats?userId=${userId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                javaBridge.log(`HTTP error! status: ${response.status}`);
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            javaBridge.log('Chats fetched successfully');

            const chats = await response.json();
            this.renderChats(chats);

        } catch (error) {
            javaBridge.log('Error loading chats: ' + error);
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
        // Log para diagnostico
        javaBridge.log('Chat object received: ' + JSON.stringify(chat));

        const initials = this.getInitials(chat.name);
        const time = chat.lastMessageTime ? Utils.formatRelative(chat.lastMessageTime) : '';
        const unreadBadge = chat.unreadCount > 0
            ? `<span class="chat-item-unread">${chat.unreadCount}</span>`
            : '';

        return `
            <div class="chat-item" onclick="Home.openChat('${chat.id}')">
                <div class="chat-item-avatar">
                    ${chat.image
                        ? `<img src="${chat.image}" alt="${chat.name}">`
                        : initials}
                </div>
                <div class="chat-item-content">
                    <div class="chat-item-header">
                        <span class="chat-item-name">${Utils.escapeHtml(chat.name)}</span>
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
        if (typeof Bridge !== 'undefined') {
            Bridge.log('Opening chat: ' + chatId);
        }

        // Actualizar params para que el chat sepa cual abrir
        if (typeof loadPage === 'function') {
            // Guardar chatId en sesion via bridge si es posible
            loadPage('chat');
        }
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
Bridge.whenReady(() => Home.init());
