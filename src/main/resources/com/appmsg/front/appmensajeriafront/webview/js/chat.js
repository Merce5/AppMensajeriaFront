/**
 * Chat - Logica de la vista de chat
 */
let Chat = {
    messages: [],
    attachments: [],
    typingTimeout: null,
    isTyping: false,
    userId: null,
    chatId: null,
    initialized: false,
    chatInfo: null,
    loadingMessages: false,
    activeEmojiCategory: 'recent',
    emojiRecentKey: 'appmsg_recent_emojis',
    emojiCategories: [
        { id: 'recent', label: 'Recientes', icon: '🕘', emojis: [] },
        { id: 'smileys', label: 'Caras', icon: '😀', emojis: ['😀','😃','😄','😁','😆','😂','🤣','😊','😍','😘','😎','🥳','😅','🙂','🙃','😉','😋','😜','🤔','😐','😶','😴','😢','😭','😡','😱','🤯','🥺'] },
        { id: 'gestures', label: 'Gestos', icon: '👍', emojis: ['👍','👎','👌','👏','🙌','🙏','🤝','💪','👋','🤙','✌️','🤞','🫶','☝️','👇','👈','👉','✍️','🤌','🫡'] },
        { id: 'hearts', label: 'Amor', icon: '❤️', emojis: ['❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💔','💕','💞','💓','💗','💖','💘','💝','💟'] },
        { id: 'things', label: 'Varios', icon: '✨', emojis: ['✨','🔥','⭐','🌟','💫','🎉','🎊','✅','❌','⚠️','💯','📌','📎','📷','🎧','☕','🍕','🍔','⚽','🎮','🚀','💡'] }
    ],

    // ==================== INICIALIZACION ====================

    init: function() {
        Bridge.log("Initializing Chat view");

        if (window.javaBridge && typeof javaBridge.loadSettings === "function") {
            javaBridge.loadSettings();
        }

        this.userId = Bridge.getUserId();
        this.chatId = Bridge.getChatId();

        if (!this.chatId) {
            console.error('No chatId provided');
            Bridge.log('No chatId provided, cannot initialize chat');
            return;
        }

        Bridge.log('User ID: ' + this.userId);
        Bridge.log('Chat ID: ' + this.chatId);

        Bridge.connectToChat(this.chatId);
        this.loadChatInfo();
        this.setupEventListeners();
        this.loadMessages();

        Bridge.log('Chat initialized: ' + this.chatId);
        this.initialized = true;
    },

    // Recibe los ajustes y aplica el tema
    onSettingsLoaded: function(dto) {
        if (dto) {
            Utils.applyTheme(dto);
        }
        // Aplicar fondo de chat si hay wallpaper
        if (dto && dto.wallpaperPath) {
            const wallpaperUrl = Bridge.resolveFileUrl(dto.wallpaperPath);
            const messages = document.getElementById('messages');
            if (messages) {
                messages.style.backgroundImage = `url('${wallpaperUrl}')`;
                messages.style.backgroundSize = 'cover';
                messages.style.backgroundPosition = 'center';
                messages.style.backgroundRepeat = 'no-repeat';
            }
        } else {
            const messages = document.getElementById('messages');
            if (messages) {
                messages.style.backgroundImage = '';
            }
        }
    },

    setupEventListeners: function() {
        const input = document.getElementById('message-input');
        if (input) {
            input.addEventListener('focus', () => this.scrollToBottom());
        }

        this.renderEmojiPicker();

        document.addEventListener('mousedown', (event) => this.closeEmojiPickerFromOutside(event), true);
        document.addEventListener('touchstart', (event) => this.closeEmojiPickerFromOutside(event), true);
        document.addEventListener('click', (event) => this.closeEmojiPickerFromOutside(event));

        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                this.closeEmojiPicker();
            }
        });
    },

    loadMessages: function() {
        this.fetchMessages();
    },

    // ==================== CHAT INFO / TOOLBAR ====================
    fetchMessages: async function() {
        if (this.loadingMessages) return;

        this.loadingMessages = true;

        try {
            const messages = await Bridge.loadMessages(this.chatId);

            if (messages && messages.length > 0) {
                const emptyState = document.getElementById('empty-state');
                if (emptyState) {
                    emptyState.classList.add('hidden');
                }

                // Limpiar mensajes actuales
                this.messages = [];

                // Renderizar todos en orden
                messages.forEach(msg => {
                    this.renderMessage(msg);
                    this.messages.push(msg);
                });

                // Scroll al final
                setTimeout(() => this.scrollToBottom(), 100);
            }

        } catch (error) {
            console.error('Error fetching messages:', error);
        } finally {
            this.loadingMessages = false;
        }
    },
    loadChatInfo: function() {
        Bridge.getChatInfo(this.chatId)
            .then(info => this.applyChatInfo(info))
            .catch(err => Bridge.log('Error cargando info del chat: ' + err));
    },

    applyChatInfo: function(info) {
        this.chatInfo = info;

        const nameEl = document.getElementById('chat-name');
        const statusEl = document.getElementById('chat-status');
        const avatarEl = document.getElementById('chat-avatar');
        const avatarPlaceholder = document.getElementById('chat-avatar-placeholder');

        if (nameEl && info.chatName) nameEl.textContent = info.chatName;

        // Subtitle: member count for groups
        if (statusEl && info.members) {
            const count = info.members.length;
            if (this._isGroupChat(info)) {
                statusEl.textContent = count + ' participante' + (count !== 1 ? 's' : '');
            }
        }

        // Avatar — resolve relative URL to full http:// URL
        const resolvedImage = info.chatImage ? Bridge.resolveFileUrl(info.chatImage) : '';
        if (avatarEl) {
            if (resolvedImage) {
                avatarEl.src = resolvedImage;
                avatarEl.style.display = '';
                if (avatarPlaceholder) avatarPlaceholder.style.display = 'none';
                avatarEl.onerror = function() {
                    this.style.display = 'none';
                    if (avatarPlaceholder) avatarPlaceholder.style.display = 'flex';
                };
            } else {
                avatarEl.style.display = 'none';
                if (avatarPlaceholder) avatarPlaceholder.style.display = 'flex';
            }
        }

        this.populateSettingsPanel(info);
    },

    // Determina si es un chat de grupo (isGroup del backend O más de 1 miembro)
    _isGroupChat: function(info) {
        if (info.isGroup) return true;
        if (info.members && info.members.length > 1) return true;
        return false;
    },

    populateSettingsPanel: function(info) {
        const sImg = document.getElementById('settings-avatar-img');
        const sPlaceholder = document.getElementById('settings-avatar-placeholder');
        const sName = document.getElementById('settings-display-name');
        const sDesc = document.getElementById('settings-display-desc');
        const sNameInput = document.getElementById('settings-name-input');
        const sDescTextarea = document.getElementById('settings-desc-textarea');
        const editSection = document.getElementById('settings-edit-section');
        const addMemberSection = document.getElementById('settings-add-member');

        const isGroup = this._isGroupChat(info);

        // Nombre en la cabecera del panel
        if (sName) sName.textContent = info.chatName || 'Chat';

        // Descripción visible bajo el nombre (solo si tiene contenido)
        if (sDesc) {
            sDesc.textContent = info.chatStatus || '';
            sDesc.style.display = info.chatStatus ? '' : 'none';
        }

        // Rellenar inputs/textarea — siempre, sin importar si es grupo
        if (sNameInput) sNameInput.value = info.chatName || '';
        if (sDescTextarea) sDescTextarea.value = info.chatStatus || '';

        // Avatar — resolver URL relativa a http://
        const resolvedImage = info.chatImage ? Bridge.resolveFileUrl(info.chatImage) : '';
        if (sImg) {
            if (resolvedImage) {
                sImg.src = resolvedImage;
                sImg.style.display = '';
                if (sPlaceholder) sPlaceholder.style.display = 'none';
                sImg.onerror = function() {
                    this.style.display = 'none';
                    if (sPlaceholder) sPlaceholder.style.display = 'flex';
                };
            } else {
                sImg.style.display = 'none';
                if (sPlaceholder) sPlaceholder.style.display = 'flex';
            }
        }

        // Sección "Configuración del grupo" (cambiar nombre) — solo para grupos
        if (editSection) editSection.style.display = isGroup ? '' : 'none';
        // Botón añadir miembro — solo para grupos
        if (addMemberSection) addMemberSection.style.display = isGroup ? '' : 'none';

        this.renderMembersList(info.members || []);
    },

    renderMembersList: function(members) {
        const countEl = document.getElementById('settings-members-count');
        const listEl = document.getElementById('settings-members-list');
        if (countEl) countEl.textContent = members.length;
        if (!listEl) return;

        listEl.innerHTML = members.map(m => {
            const initials = (m.username || '?').charAt(0).toUpperCase();
            const avatarUrl = m.picture ? Bridge.resolveFileUrl(m.picture) : '';
            const avatarHtml = m.picture
                ? `<div class="member-avatar"><img src="${Utils.escapeHtml(avatarUrl)}" alt="" onerror="this.parentElement.textContent='${initials}'"></div>`
                : `<div class="member-avatar">${initials}</div>`;
            return `<div class="member-item">${avatarHtml}<span class="member-name">${Utils.escapeHtml(m.username || 'Usuario')}</span></div>`;
        }).join('');
    },

    // ==================== SETTINGS PANEL ====================

    openSettings: function() {
        const panel = document.getElementById('settings-panel');
        if (!panel) return;
        panel.classList.remove('hidden');
        requestAnimationFrame(() => panel.classList.add('open'));

        // Load media gallery each time the panel opens
        this.loadMediaGallery();
    },

    closeSettings: function() {
        const panel = document.getElementById('settings-panel');
        if (!panel) return;
        panel.classList.remove('open');
        setTimeout(() => panel.classList.add('hidden'), 260);
    },

    changeGroupImage: function() {
        Bridge.chooseChatImage()
            .then(url => {
                if (!url) return;
                // Guardar la URL relativa en BD, pero mostrar la URL resuelta en la UI
                const displayUrl = Bridge.resolveFileUrl(url);
                return Bridge.updateChatInfo(this.chatId, { chatImage: url }).then(() => {
                    const sImg = document.getElementById('settings-avatar-img');
                    const sPlaceholder = document.getElementById('settings-avatar-placeholder');
                    if (sImg) { sImg.src = displayUrl; sImg.style.display = ''; }
                    if (sPlaceholder) sPlaceholder.style.display = 'none';
                    const avatarEl = document.getElementById('chat-avatar');
                    const avatarPl = document.getElementById('chat-avatar-placeholder');
                    if (avatarEl) { avatarEl.src = displayUrl; avatarEl.style.display = ''; }
                    if (avatarPl) avatarPl.style.display = 'none';
                    if (this.chatInfo) this.chatInfo.chatImage = url;
                });
            })
            .catch(err => Bridge.log('Error cambiando imagen: ' + err));
    },

    saveGroupName: function() {
        const input = document.getElementById('settings-name-input');
        const newName = input ? input.value.trim() : '';
        if (!newName) return;

        Bridge.updateChatInfo(this.chatId, { chatName: newName })
            .then(() => {
                if (this.chatInfo) this.chatInfo.chatName = newName;
                const nameEl = document.getElementById('chat-name');
                if (nameEl) nameEl.textContent = newName;
                const sName = document.getElementById('settings-display-name');
                if (sName) sName.textContent = newName;
            })
            .catch(err => Bridge.log('Error actualizando nombre: ' + err));
    },

    saveGroupStatus: function() {
        const textarea = document.getElementById('settings-desc-textarea');
        const newStatus = textarea ? textarea.value.trim() : '';

        Bridge.updateChatInfo(this.chatId, { chatStatus: newStatus })
            .then(() => {
                if (this.chatInfo) this.chatInfo.chatStatus = newStatus;
                const sDesc = document.getElementById('settings-display-desc');
                if (sDesc) {
                    sDesc.textContent = newStatus;
                    sDesc.style.display = newStatus ? '' : 'none';
                }
            })
            .catch(err => Bridge.log('Error actualizando descripción: ' + err));
    },

    addMember: function() {
        const input = document.getElementById('add-member-input');
        const username = input ? input.value.trim() : '';
        if (!username) return;

        Bridge.addMemberToChat(this.chatId, username)
            .then(result => {
                if (input) input.value = '';
                // Refresh chat info to update members list
                this.loadChatInfo();
            })
            .catch(err => {
                Bridge.log('Error añadiendo miembro: ' + err);
                alert('No se pudo añadir al usuario: ' + err.message);
            });
    },

    // ==================== MULTIMEDIA GALLERY ====================

    loadMediaGallery: function() {
        const gallery = document.getElementById('settings-media-gallery');
        if (!gallery) return;

        gallery.innerHTML = '<div class="media-gallery-empty">Cargando...</div>';

        Bridge.getChatMedia(this.chatId)
            .then(messages => {
                const allMedia = [];
                messages.forEach(msg => {
                    if (msg._multimedia && msg._multimedia.length > 0) {
                        msg._multimedia.forEach(url => allMedia.push(url));
                    } else if (msg.multimedia && msg.multimedia.length > 0) {
                        msg.multimedia.forEach(url => allMedia.push(url));
                    }
                });

                if (allMedia.length === 0) {
                    gallery.innerHTML = '<div class="media-gallery-empty">No hay multimedia compartida</div>';
                    return;
                }

                gallery.innerHTML = allMedia.map(rawUrl => {
                    const url = Bridge.resolveFileUrl(rawUrl);
                    if (Utils.isImage(url)) {
                        return `<div class="media-thumb" onclick="Chat.openMedia('${url}')">
                            <img src="${url}" alt="Media" loading="lazy" onerror="this.parentElement.style.display='none'">
                        </div>`;
                    } else if (Utils.isVideo(url)) {
                        return `<div class="media-thumb" onclick="Chat.openMedia('${url}')">
                            <video src="${url}" preload="metadata"></video>
                        </div>`;
                    } else {
                        const name = Utils.getFileName(rawUrl);
                        const icon = Utils.getFileIcon(rawUrl);
                        return `<div class="media-thumb media-thumb-file" onclick="Chat.openMedia('${url}')">
                            <span>${icon}</span>
                            <span>${Utils.escapeHtml(name)}</span>
                        </div>`;
                    }
                }).join('');
            })
            .catch(() => {
                gallery.innerHTML = '<div class="media-gallery-empty">No hay multimedia compartida</div>';
            });
    },

    // ==================== BORRAR CHAT ====================
    deleteChat: function() {
            Bridge.deleteChat(this.chatId)
                .then(result => {})
                .catch(err => {
                    Bridge.log('Error borrando chat: ' + err);
                    alert('No se pudo borrar el chat: ' + err.message);
                });
        },

    goBack: function () {
            Bridge.navigate("main.html")
            },

    // ==================== ENVIO DE MENSAJES ====================

    closeEmojiPickerFromOutside: function(event) {
        const picker = document.getElementById('emoji-picker');
        const wrapper = document.querySelector('.emoji-menu-wrapper');
        if (!picker || !wrapper || picker.classList.contains('hidden')) return;

        const target = event.target;
        if (target && wrapper.contains(target)) return;

        this.closeEmojiPicker();
    },

    toggleEmojiPicker: function(event) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }

        const picker = document.getElementById('emoji-picker');
        if (!picker) return;

        if (picker.classList.contains('hidden')) {
            this.openEmojiPicker();
        } else {
            this.closeEmojiPicker();
        }
    },

    openEmojiPicker: function() {
        const picker = document.getElementById('emoji-picker');
        const toggle = document.getElementById('emoji-toggle');
        if (!picker) return;

        this.renderEmojiPicker();
        picker.classList.remove('hidden');
        if (toggle) toggle.classList.add('active');
    },

    closeEmojiPicker: function() {
        const picker = document.getElementById('emoji-picker');
        const toggle = document.getElementById('emoji-toggle');
        if (picker) picker.classList.add('hidden');
        if (toggle) toggle.classList.remove('active');
    },

    renderEmojiPicker: function() {
        const tabs = document.getElementById('emoji-tabs');
        const grid = document.getElementById('emoji-grid');
        if (!tabs || !grid) return;

        const categories = this.emojiCategories.map(category => {
            if (category.id === 'recent') {
                return Object.assign({}, category, { emojis: this.getRecentEmojis() });
            }
            return category;
        });

        const activeCategory = categories.find(category => category.id === this.activeEmojiCategory);
        if (!activeCategory || (activeCategory.id === 'recent' && activeCategory.emojis.length === 0)) {
            this.activeEmojiCategory = 'smileys';
        }

        tabs.innerHTML = '';
        categories.forEach(category => {
            if (category.id === 'recent' && category.emojis.length === 0) return;

            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'emoji-tab' + (category.id === this.activeEmojiCategory ? ' active' : '');
            button.title = category.label;
            button.textContent = category.icon;
            button.addEventListener('click', (event) => {
                event.stopPropagation();
                this.activeEmojiCategory = category.id;
                this.renderEmojiPicker();
            });
            tabs.appendChild(button);
        });

        const selected = categories.find(category => category.id === this.activeEmojiCategory) || categories[1];
        grid.innerHTML = '';
        selected.emojis.forEach(emoji => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'emoji-option';
            button.textContent = emoji;
            button.title = emoji;
            button.addEventListener('click', (event) => {
                event.preventDefault();
                event.stopPropagation();
                this.insertEmoji(emoji);
            });
            grid.appendChild(button);
        });
    },

    insertEmoji: function(emoji) {
        const input = document.getElementById('message-input');
        if (!input) return;

        const start = typeof input.selectionStart === 'number' ? input.selectionStart : input.value.length;
        const end = typeof input.selectionEnd === 'number' ? input.selectionEnd : start;

        input.focus();
        if (typeof input.setRangeText === 'function') {
            input.setRangeText(emoji, start, end, 'end');
        } else {
            input.value = input.value.substring(0, start) + emoji + input.value.substring(end);
            const cursor = start + emoji.length;
            input.setSelectionRange(cursor, cursor);
        }

        this.saveRecentEmoji(emoji);
        this.handleTyping({ key: emoji });
    },

    getRecentEmojis: function() {
        try {
            const stored = localStorage.getItem(this.emojiRecentKey);
            const parsed = stored ? JSON.parse(stored) : [];
            return Array.isArray(parsed) ? parsed.slice(0, 18) : [];
        } catch (e) {
            return [];
        }
    },

    saveRecentEmoji: function(emoji) {
        const recent = this.getRecentEmojis().filter(item => item !== emoji);
        recent.unshift(emoji);
        try {
            localStorage.setItem(this.emojiRecentKey, JSON.stringify(recent.slice(0, 18)));
        } catch (e) {
            Bridge.log('No se pudieron guardar emojis recientes: ' + e);
        }
        this.renderEmojiPicker();
    },

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

        this.closeEmojiPicker();

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
        // No mostrar evento propio
        const eventUserId = event.userId || event.senderId || '';
        if (eventUserId === this.userId) return;

        const container = document.getElementById('messages');
        const shouldScroll = Utils.isNearBottom(container);

        const displayName = event.username || eventUserId;
        const time = event.timestamp ? Utils.formatTime(event.timestamp) : '';

        const div = document.createElement('div');
        div.className = 'system-message';
        div.innerHTML = `<span>${Utils.escapeHtml(displayName)} ${action}</span>`
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
        Bridge.log('senderId ' + msg.senderId + ' userId ' + this.userId + ' isOwn ' + (msg.senderId === this.userId))
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
        const typingUserId = data.userId || data.senderId || '';

        if (data.isTyping && typingUserId !== this.userId) {
            userSpan.textContent = data.username || 'Alguien';
            indicator.classList.remove('hidden');
        } else if (!data.isTyping || typingUserId === this.userId) {
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
        if (!status) return;

        // Para grupos mostramos el conteo de miembros en lugar del estado de conexión
        if (this.chatInfo && this.chatInfo.isGroup && this.chatInfo.members) {
            const count = this.chatInfo.members.length;
            status.textContent = count + ' participante' + (count !== 1 ? 's' : '');
            status.className = 'chat-status';
        } else if (connected) {
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
