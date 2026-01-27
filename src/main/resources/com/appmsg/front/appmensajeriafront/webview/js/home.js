/**
 * Home - Pagina principal con lista de chats
 */
const Home = {

    chats: [],

    init: function() {
        this.loadChats();
    },

    loadChats: function() {
        // Por ahora mostramos el estado vacio o algunos chats de ejemplo
        // En una implementacion real, esto llamaria a un endpoint del backend

        // Simular carga
        setTimeout(() => {
            // Por ahora mostrar estado vacio
            // Cuando tengas el endpoint de listar chats, descomentar:
            // this.fetchChats();

            this.showEmptyState();
        }, 500);
    },

    fetchChats: async function() {
        try {
            // TODO: Implementar cuando el backend tenga endpoint de listar chats
            // const chats = await Bridge.getChats();
            // this.renderChats(chats);

            this.showEmptyState();
        } catch (error) {
            console.error('Error loading chats:', error);
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
