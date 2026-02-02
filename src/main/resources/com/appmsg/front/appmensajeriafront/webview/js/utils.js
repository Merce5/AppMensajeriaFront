/**
 * Utilidades comunes para todas las vistas
 */
const Utils = {

    // ==================== FORMATEO ====================

    /**
     * Formatea un timestamp a hora (HH:MM)
     */
    formatTime: function(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('es-ES', {
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    /**
     * Formatea un timestamp a fecha
     */
    formatDate: function(timestamp) {
        const date = new Date(timestamp);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        if (date.toDateString() === today.toDateString()) {
            return 'Hoy';
        } else if (date.toDateString() === yesterday.toDateString()) {
            return 'Ayer';
        } else {
            return date.toLocaleDateString('es-ES', {
                day: 'numeric',
                month: 'short',
                year: date.getFullYear() !== today.getFullYear() ? 'numeric' : undefined
            });
        }
    },

    /**
     * Formatea timestamp relativo (hace X minutos)
     */
    formatRelative: function(timestamp) {
        const now = Date.now();
        const diff = now - timestamp;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return 'Ahora';
        if (minutes < 60) return `Hace ${minutes} min`;
        if (hours < 24) return `Hace ${hours}h`;
        if (days < 7) return `Hace ${days}d`;
        return this.formatDate(timestamp);
    },

    // ==================== ARCHIVOS ====================

    /**
     * Obtiene el nombre de archivo de un path
     */
    getFileName: function(path) {
        return path.split(/[\\/]/).pop();
    },

    /**
     * Verifica si un archivo es imagen
     */
    isImage: function(url) {
        return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
    },

    /**
     * Verifica si un archivo es video
     */
    isVideo: function(url) {
        return /\.(mp4|webm|ogg|mov)$/i.test(url);
    },

    /**
     * Verifica si un archivo es documento
     */
    isDocument: function(url) {
        return /\.(pdf|doc|docx|xls|xlsx|txt)$/i.test(url);
    },

    /**
     * Obtiene el icono según tipo de archivo
     */
    getFileIcon: function(url) {
        if (this.isImage(url)) return '🖼️';
        if (this.isVideo(url)) return '🎬';
        if (/\.pdf$/i.test(url)) return '📄';
        if (/\.(doc|docx)$/i.test(url)) return '📝';
        if (/\.(xls|xlsx)$/i.test(url)) return '📊';
        return '📎';
    },

    // ==================== TEXTO ====================

    /**
     * Escapa HTML para prevenir XSS
     */
    escapeHtml: function(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },

    /**
     * Convierte URLs en enlaces
     */
    linkify: function(text) {
        const urlRegex = /(https?:\/\/[^\s]+)/g;
        return text.replace(urlRegex, '<a href="$1" target="_blank">$1</a>');
    },

    // ==================== DOM ====================

    /**
     * Aplica el tema global (oscuro o claro) en toda la app
     * @param {boolean} darkMode
     */
    applyTheme: function(darkMode) {
        document.documentElement.dataset.theme = darkMode ? "dark" : "light";
    },

    /**
     * Scroll al final de un elemento
     */
    scrollToBottom: function(element) {
        if (element) {
            element.scrollTop = element.scrollHeight;
        }
    },

    /**
     * Verifica si el scroll está cerca del final
     */
    isNearBottom: function(element, threshold = 100) {
        if (!element) return true;
        return element.scrollHeight - element.scrollTop - element.clientHeight < threshold;
    },

    // ==================== ICONOS SVG ====================

    icons: {
        back: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>`,

        send: `<svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
        </svg>`,

        attach: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/>
        </svg>`,

        close: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
        </svg>`,

        check: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 6L9 17l-5-5"/>
        </svg>`,

        doubleCheck: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L9 17l-5-5"/>
            <path d="M22 6l-9 11"/>
        </svg>`,

        user: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
        </svg>`,

        image: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <path d="M21 15l-5-5L5 21"/>
        </svg>`,

        file: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
            <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8"/>
        </svg>`,

        chat: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
        </svg>`
    },

    /**
     * Obtiene un icono SVG
     */
    getIcon: function(name) {
        return this.icons[name] || '';
    }
};
