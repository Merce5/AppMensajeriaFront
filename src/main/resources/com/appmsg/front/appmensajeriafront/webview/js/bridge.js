/**
 * Bridge - Wrapper para comunicación con Java
 * javaBridge es inyectado por WebViewManager
 */
const Bridge = {

    // ==================== ESTADO ====================

    isReady: function() {
        return typeof javaBridge !== 'undefined';
    },

    whenReady: function(callback) {
        if (this.isReady()) {
            callback();
        } else {
            window.onBridgeReady = callback;
        }
    },

    // ==================== DATOS DE SESIÓN ====================

    getUserId: function() {
        if (!this.isReady()) return null;
        return javaBridge.getUserId();
    },

    getChatId: function() {
        if (!this.isReady()) return null;
        return javaBridge.getChatId();
    },

    getInitParams: function() {
        if (!this.isReady()) return {};
        try {
            const paramsJson = javaBridge.getInitParams();
            return JSON.parse(paramsJson || '{}');
        } catch (e) {
            console.error('Error parsing init params:', e);
            return {};
        }
    },

    // ==================== CHAT / WEBSOCKET ====================

    connectToChat: function(chatId) {
        if (!this.isReady()) return;
        javaBridge.connectToChat(chatId);
    },

    sendMessage: function(text, multimedia) {
        if (!this.isReady()) return;
        const multimediaJson = JSON.stringify(multimedia || []);
        javaBridge.sendMessage(text, multimediaJson);
    },

    sendTyping: function(isTyping) {
        if (!this.isReady()) return;
        javaBridge.sendTypingIndicator(isTyping);
    },

    disconnectChat: function() {
        if (!this.isReady()) return;
        javaBridge.disconnectChat();
    },

    // ==================== FILE CHOOSER ====================

    openFileChooser: function(filterType) {
        if (!this.isReady()) return [];
        try {
            const result = javaBridge.openFileChooser(filterType || 'all');
            return result ? JSON.parse(result) : [];
        } catch (e) {
            console.error('Error opening file chooser:', e);
            return [];
        }
    },

    // ==================== UPLOAD (async) ====================

    uploadFiles: function(filePaths) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error('Bridge not ready'));
                return;
            }

            const callbackName = '_uploadCallback_' + Date.now();
            window[callbackName] = function(responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response.success) {
                        resolve(response);
                    } else {
                        reject(new Error(response.error || 'Upload failed'));
                    }
                } catch (e) {
                    reject(e);
                }
            };

            javaBridge.uploadFiles(JSON.stringify(filePaths), callbackName);
        });
    },

    // ==================== PROFILE (async) ====================

    loadProfile: function(userId) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error('Bridge not ready'));
                return;
            }

            const callbackName = '_profileCallback_' + Date.now();
            window[callbackName] = function(responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response.error) {
                        reject(new Error(response.error));
                    } else {
                        resolve(response);
                    }
                } catch (e) {
                    reject(e);
                }
            };

            javaBridge.loadProfile(userId, callbackName);
        });
    },

    // ==================== INVITE (async) ====================

    joinByInvite: function(inviteCode) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error('Bridge not ready'));
                return;
            }

            const callbackName = '_inviteCallback_' + Date.now();
            window[callbackName] = function(responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response.success) {
                        resolve(response);
                    } else {
                        reject(new Error(response.message || 'Join failed'));
                    }
                } catch (e) {
                    reject(e);
                }
            };

            javaBridge.joinByInvite(inviteCode, callbackName);
        });
    },

    // ==================== NAVEGACIÓN ====================

    navigateTo: function(page) {
        if (!this.isReady()) return;
        javaBridge.navigateTo(page);
    },

    goBack: function() {
        // Primero intentar navegación JS, luego Java
        if (typeof window.goBack === 'function') {
            window.goBack();
        } else if (this.isReady()) {
            javaBridge.goBack();
        }
    },

    // ==================== LOG ====================

    log: function(message) {
        if (this.isReady()) {
            javaBridge.log(message);
        }
        console.log(message);
    }
};

// ==================== CALLBACKS GLOBALES ====================
// Estos son llamados desde Java cuando hay eventos

window.onMessageReceived = function(messageJson) {
    try {
        const message = JSON.parse(messageJson);
        if (typeof Chat !== 'undefined' && Chat.onMessageReceived) {
            Chat.onMessageReceived(message);
        }
    } catch (e) {
        console.error('Error parsing message:', e);
    }
};

window.onTypingReceived = function(typingJson) {
    try {
        const data = JSON.parse(typingJson);
        if (typeof Chat !== 'undefined' && Chat.onTypingReceived) {
            Chat.onTypingReceived(data);
        }
    } catch (e) {
        console.error('Error parsing typing:', e);
    }
};

window.onConnectionStatusChanged = function(connected) {
    const isConnected = connected === 'true' || connected === true;
    if (typeof Chat !== 'undefined' && Chat.onConnectionStatusChanged) {
        Chat.onConnectionStatusChanged(isConnected);
    }
};
