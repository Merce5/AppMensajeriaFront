/**
 * Chat - Logica de la vista de chat
 */
const Chat = {
    messages: [],
    attachments: [],
    typingTimeout: null,
    isTyping: false,
    userId: null,
    chatId: null,
    initialized: false,

    // ==================== INICIALIZACION ====================

    init: function() {
        Bridge.log("Initializing Chat view");

        this.userId = Bridge.getUserId();
        this.chatId = Bridge.getChatId();

        if (!this.chatId) {
            console.error('No chatId provided');
            Bridge.log('No chatId provided, cannot initialize chat');
            return;
        }

        Bridge.log('User ID: ' + this.userId);
        Bridge.log('Chat ID: ' + this.chatId);

        // Conectar al WebSocket
        Bridge.connectToChat(this.chatId);

        Bridge.log('Connected to chat: ' + this.chatId);

        // Setup
        this.setupEventListeners();
        this.loadMessages();

        Bridge.log('Chat initialized: ' + this.chatId);

        this.initialized = true;
    },

    setupEventListeners: function() {
        const input = document.getElementById('message-input');
        if (input) {
            input.addEventListener('focus', () => this.scrollToBottom());
        }
    },

    loadMessages: function() {
        // Los mensajes llegan por WebSocket
        // Aqui podriamos cargar historial si hubiera endpoint
    },

    // ==================== ENVIO DE MENSAJES ====================

    sendMessage: function() {
        const input = document.getElementById('message-input');
        const text = input.value.trim();

        if (!text && this.attachments.length === 0) return;

        // Si hay archivos, subirlos primero
        if (this.attachments.length > 0) {
            this.uploadAndSend(text);
        } else {
            Bridge.sendMessage(text, []);
            input.value = '';
        }

        // Limpiar estado de typing
        this.stopTyping();
    },

    uploadAndSend: async function(text) {
        const sendBtn = document.querySelector('.btn-send');
        sendBtn.disabled = true;

        try {
            const response = await Bridge.uploadFiles(this.attachments);
            let urls = [];

            if (response.files) {
                // El backend devuelve files como string JSON
                try {
                    urls = JSON.parse(response.files);
                } catch (e) {
                    urls = response.files;
                }
            }

            Bridge.sendMessage(text, urls);

            // Limpiar
            this.attachments = [];
            this.updateAttachmentsPreview();
            document.getElementById('message-input').value = '';

        } catch (error) {
            console.error('Error uploading files:', error);
            alert('Error al subir archivos: ' + error.message);
        } finally {
            sendBtn.disabled = false;
        }
    },

    // ==================== RECEPCION DE MENSAJES ====================

    onMessageReceived: function(message) {
        const type = message.type || 'message';

        switch (type) {
            case 'message':
                this.handleChatMessage(message);
                break;
            case 'user_connected':
                this.handleSystemEvent(message, 'se ha conectado');
                break;
            case 'user_disconnected':
                this.handleSystemEvent(message, 'se ha desconectado');
                break;
            case 'status':
                this.handleStatusUpdate(message);
                break;
            case 'error':
                this.handleError(message);
                break;
            default:
                Bridge.log('Tipo de mensaje desconocido: ' + type);
                break;
        }
    },

    handleChatMessage: function(message) {
        // Ocultar estado vacio
        const emptyState = document.getElementById('empty-state');
        if (emptyState) {
            emptyState.classList.add('hidden');
        }

        const container = document.getElementById('messages');
        const shouldScroll = Utils.isNearBottom(container);

        this.renderMessage(message);

        if (shouldScroll) {
            this.scrollToBottom();
        }

        this.messages.push(message);
    },

    handleSystemEvent: function(event, action) {
        const container = document.getElementById('messages');
        const shouldScroll = Utils.isNearBottom(container);

        const userId = event.userId || event.senderId || '';
        const time = event.timestamp ? Utils.formatTime(event.timestamp) : '';

        const div = document.createElement('div');
        div.className = 'system-message';
        div.innerHTML = `<span>${Utils.escapeHtml(userId)} ${action}</span>`
            + (time ? `<span class="system-message-time">${time}</span>` : '');

        container.appendChild(div);

        if (shouldScroll) {
            this.scrollToBottom();
        }
    },

    handleStatusUpdate: function(data) {
        // Buscar el mensaje en el DOM y actualizar su icono de estado
        const messageId = data.messageId;
        const newStatus = data.status;
        if (!messageId || !newStatus) return;

        const msgEl = document.querySelector(`[data-message-id="${messageId}"]`);
        if (msgEl) {
            const metaEl = msgEl.querySelector('.status-icon');
            if (metaEl) {
                metaEl.className = 'status-icon' + (newStatus === 'read' ? ' read' : '');
                metaEl.innerHTML = newStatus === 'sent' ? '&#10003;' : '&#10003;&#10003;';
            }
        }

        // Actualizar en el array tambien
        const stored = this.messages.find(m => m.messageId === messageId);
        if (stored) {
            stored.status = newStatus;
        }
    },

    handleError: function(data) {
        const errorMsg = data.message || 'Error desconocido';
        Bridge.log('WebSocket error: ' + errorMsg);

        const container = document.getElementById('messages');
        const div = document.createElement('div');
        div.className = 'system-message system-message-error';
        div.innerHTML = `<span>${Utils.escapeHtml(errorMsg)}</span>`;
        container.appendChild(div);
    },

    renderMessage: function(msg) {
        const container = document.getElementById('messages');
        const isOwn = msg.senderId === this.userId;

        const div = document.createElement('div');
        div.className = 'message ' + (isOwn ? 'message-own' : 'message-other');
        div.dataset.messageId = msg.messageId || '';

        let html = '';

        // Nombre del remitente (solo en mensajes de otros)
        if (!isOwn && msg.username) {
            html += `<div class="message-sender">${Utils.escapeHtml(msg.username)}</div>`;
        }

        // Multimedia
        if (msg.multimedia && msg.multimedia.length > 0) {
            html += '<div class="message-media">';
            msg.multimedia.forEach(rawUrl => {
                const url = Bridge.resolveFileUrl(rawUrl);
                if (Utils.isImage(url)) {
                    html += `<img src="${url}" class="media-image" onclick="Chat.openMedia('${url}')" alt="Imagen">`;
                } else if (Utils.isVideo(url)) {
                    html += `<video src="${url}" class="media-video" controls></video>`;
                } else {
                    const fileName = Utils.getFileName(rawUrl);
                    const icon = Utils.getFileIcon(rawUrl);
                    html += `<div class="media-file" onclick="Chat.openMedia('${url}')" style="cursor:pointer">
                        <span>${icon}</span>
                        <span>${Utils.escapeHtml(fileName)}</span>
                    </div>`;
                }
            });
            html += '</div>';
        }

        // Texto
        if (msg.message) {
            const escapedText = Utils.escapeHtml(msg.message);
            const linkedText = Utils.linkify(escapedText);
            html += `<p class="message-text">${linkedText}</p>`;
        }

        // Timestamp y estado
        const time = msg.timestamp ? Utils.formatTime(msg.timestamp) : '';
        const statusIcon = isOwn ? this.getStatusIcon(msg.status) : '';
        html += `<span class="message-meta">${time} ${statusIcon}</span>`;

        div.innerHTML = html;
        container.appendChild(div);
    },

    getStatusIcon: function(status) {
        switch (status) {
            case 'sent':
                return '<span class="status-icon">✓</span>';
            case 'delivered':
                return '<span class="status-icon">✓✓</span>';
            case 'read':
                return '<span class="status-icon read">✓✓</span>';
            default:
                return '';
        }
    },

    // ==================== TYPING INDICATOR ====================

    handleTyping: function(event) {
        // No enviar typing si es Enter o teclas especiales
        if (event.key === 'Enter') return;

        if (!this.isTyping) {
            this.isTyping = true;
            Bridge.sendTyping(true);
        }

        // Reset timeout
        clearTimeout(this.typingTimeout);
        this.typingTimeout = setTimeout(() => this.stopTyping(), 2000);
    },

    stopTyping: function() {
        if (this.isTyping) {
            this.isTyping = false;
            Bridge.sendTyping(false);
        }
        clearTimeout(this.typingTimeout);
    },

    onTypingReceived: function(data) {
        const indicator = document.getElementById('typing-indicator');
        const userSpan = document.getElementById('typing-user');

        if (data.isTyping && data.senderId !== this.userId) {
            userSpan.textContent = data.username || 'Alguien';
            indicator.classList.remove('hidden');
        } else {
            indicator.classList.add('hidden');
        }
    },

    handleKeyPress: function(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    },

    // ==================== ARCHIVOS ADJUNTOS ====================

    openFilePicker: function() {
        const files = Bridge.openFileChooser('all');
        if (files && files.length > 0) {
            this.attachments = this.attachments.concat(files);
            this.updateAttachmentsPreview();
        }
    },

    updateAttachmentsPreview: function() {
        const container = document.getElementById('attachments-preview');

        if (this.attachments.length === 0) {
            container.classList.add('hidden');
            container.innerHTML = '';
            return;
        }

        container.classList.remove('hidden');
        container.innerHTML = this.attachments.map((file, index) => {
            const fileName = Utils.getFileName(file);
            const icon = Utils.getFileIcon(file);
            return `
                <div class="attachment-item">
                    <span>${icon}</span>
                    <span>${Utils.escapeHtml(fileName)}</span>
                    <button class="attachment-remove" onclick="Chat.removeAttachment(${index})">
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M18 6L6 18M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
            `;
        }).join('');
    },

    removeAttachment: function(index) {
        this.attachments.splice(index, 1);
        this.updateAttachmentsPreview();
    },

    // ==================== CONEXION ====================

    onConnectionStatusChanged: function(connected) {
        const status = document.getElementById('chat-status');
        if (connected) {
            status.textContent = 'Conectado';
            status.className = 'chat-status online';
        } else {
            status.textContent = 'Reconectando...';
            status.className = 'chat-status';
        }
    },

    // ==================== NAVEGACION ====================

    openProfile: function() {
        // Navegar a la vista de perfil
        Bridge.navigate('profile.html');
    },

    openMedia: function(url) {
        // Abrir en el navegador del sistema
        Bridge.openExternal(url);
    },

    // ==================== UTILIDADES ====================

    scrollToBottom: function() {
        const container = document.getElementById('messages');
        Utils.scrollToBottom(container);
    }
};

// Inicializar cuando el bridge este listo
Bridge.whenReady(() => {
    Bridge.log("Chat page initialized");
    Chat.init()
});
