/* Bridge */

const Bridge = {
    _readyCallbacks: [],
    _bridgeReady: false,

    isReady() {
        return typeof window.javaBridge !== "undefined";
    },

    whenReady(callback) {
        if (this.isReady() || this._bridgeReady) {
            callback();
        } else {
            this._readyCallbacks.push(callback);
        }
    },

    _executeReadyCallbacks() {
        this._bridgeReady = true;
        while (this._readyCallbacks.length > 0) {
            const cb = this._readyCallbacks.shift();
            try { cb(); } catch (e) { console.error('Error in whenReady callback:', e); }
        }
    },

    getInitParams() {
        if (!this.isReady() || typeof javaBridge.getInitParams !== "function") return {};
        try {
            const json = javaBridge.getInitParams();
            return json ? JSON.parse(json) : {};
        } catch (e) {
            console.warn("Error parsing init params", e);
            return {};
        }
    },

    getChats(){
        if (!this.isReady() || typeof javaBridge.getChats !== "function") return;
        try {
            javaBridge.getChats();
        } catch (e) {
            console.warn("Error calling getChats", e);
        }
    },

    log(message) {
        try {
            if (this.isReady() && typeof javaBridge.log === "function") javaBridge.log(message);
        } catch (_) {
        }
        console.log(message);
    },

    getUserId() {
        if (!this.isReady() || typeof javaBridge.getUserId !== "function") return null;
        try {
            return javaBridge.getUserId();
        } catch (_) {
            return null;
        }
    },

    getChatId() {
        if (!this.isReady()) return null;
        const fn = javaBridge.getChatId;
        if (typeof fn !== "function") return null;
        try {
            return fn.call(javaBridge);
        } catch (_) {
            return null;
        }
    },

    setChatId(chatId) {
        if (!this.isReady()) return;
        const fn = javaBridge.setChatId;
        if (typeof fn !== "function") return;
        try { fn.call(javaBridge, chatId); } catch (_) {}
    },

    /**
     * Devuelve la URL base del backend (ej: http://localhost:8080/APPMensajeriaUEM_war_exploded)
     */
    getBaseUrl() {
        if (!this._baseUrl) {
            try {
                if (this.isReady() && typeof javaBridge.getBaseUrl === "function") {
                    this._baseUrl = javaBridge.getBaseUrl();
                }
            } catch (_) {}
            if (!this._baseUrl) this._baseUrl = "";
        }
        return this._baseUrl;
    },

    /**
     * Resuelve una URL de archivo del backend a su URL completa.
     * Si ya es absoluta la devuelve tal cual.
     */
    resolveFileUrl(path) {
        if (!path) return "";
        // Ya es absoluta
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        // Relativa al backend
        var base = this.getBaseUrl();
        if (base && !path.startsWith("/")) path = "/" + path;
        return base + path;
    },

    tryToLogin(username, password) {
        if (!this.isReady()) return;
        if (typeof javaBridge.tryToLogin === "function") {
            javaBridge.tryToLogin(username, password);
        }
    },
    register(username, password) {
        if (!this.isReady()) return;
        if (typeof javaBridge.register === "function") {
            javaBridge.register(username, password);
        }

        this.log(javaBridge.register)
    },
    verifyRegister(code) {
        if (!this.isReady()) return;
        try {
            if (typeof javaBridge.verifyRegister === "function") {
                javaBridge.verifyRegister(code);
            }
        } catch (e) {

        }
    },

    navigate(page) {
        if (!this.isReady()) return;

        if (typeof javaBridge.navigate === "function") {
            javaBridge.navigate(page);
        }
    },

    /**
     * Abre una URL en el navegador del sistema (para archivos y media).
     */
    openExternal(url) {
        if (!this.isReady() || typeof javaBridge.openExternal !== "function") return;
        try {
            javaBridge.openExternal(url);
        } catch (e) {
            console.error("Error opening external:", e);
        }
    },

    goBack() {
        if (this.isReady() && typeof javaBridge.navigate === "function") {
            javaBridge.navigate('main.html');
        }
    },

    connectToChat(chatId) {
        if (!this.isReady() || typeof javaBridge.connectToChat !== "function") return;
        javaBridge.connectToChat(chatId);
    },

    sendMessage(text, multimedia) {
        if (!this.isReady() || typeof javaBridge.sendMessage !== "function") return;
        const multimediaJson = JSON.stringify(multimedia || []);
        javaBridge.sendMessage(text, multimediaJson);
    },

    sendTyping(isTyping) {
        if (!this.isReady()) return;
        if (typeof javaBridge.sendTyping === "function") {
            javaBridge.sendTyping(!!isTyping);
            return;
        }
        if (typeof javaBridge.sendTypingIndicator === "function") {
            javaBridge.sendTypingIndicator(!!isTyping);
        }
    },

    disconnectChat() {
        if (!this.isReady() || typeof javaBridge.disconnectChat !== "function") return;
        javaBridge.disconnectChat();
    },

    openFileChooser(filterType) {
        if (!this.isReady()) return [];
        const fn = javaBridge.openFileChooser;
        if (typeof fn !== "function") return [];
        try {
            const result = fn.call(javaBridge, filterType || "all");
            return result ? JSON.parse(result) : [];
        } catch (e) {
            console.error("Error opening file chooser:", e);
            return [];
        }
    },

    uploadFiles(filePaths) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error("Bridge not ready"));
                return;
            }

            const fn = javaBridge.uploadFiles;
            if (typeof fn !== "function") {
                reject(new Error("uploadFiles not available"));
                return;
            }

            const callbackName = "_uploadCallback_" + Date.now();
            window[callbackName] = function (responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response.success) resolve(response);
                    else reject(new Error(response.error || "Upload failed"));
                } catch (e) {
                    reject(e);
                }
            };

            try {
                fn.call(javaBridge, JSON.stringify(filePaths || []), callbackName);
            } catch (e) {
                delete window[callbackName];
                reject(e);
            }
        });
    },

    loadProfile(userId) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error("Bridge not ready"));
                return;
            }

            const fn = javaBridge.loadProfile || javaBridge.getProfile;
            if (typeof fn !== "function") {
                reject(new Error("loadProfile/getProfile not available"));
                return;
            }

            const callbackName = "_profileCallback_" + Date.now();
            window[callbackName] = function (responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response && response.error) reject(new Error(response.error));
                    else resolve(response);
                } catch (e) {
                    reject(e);
                }
            };

            try {
                if (fn === javaBridge.getProfile) fn.call(javaBridge, callbackName);
                else fn.call(javaBridge, userId, callbackName);
            } catch (e) {
                delete window[callbackName];
                reject(e);
            }
        });
    },

    joinByInvite(inviteCode) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error("Bridge not ready"));
                return;
            }

            const fn = javaBridge.joinByInvite || javaBridge.joinByCode || javaBridge.sendInvite;
            if (typeof fn !== "function") {
                reject(new Error("join method not available"));
                return;
            }

            const callbackName = "_inviteCallback_" + Date.now();
            window[callbackName] = function (responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response && response.success) resolve(response);
                    else reject(new Error((response && (response.message || response.error)) || "Join failed"));
                } catch (e) {
                    reject(e);
                }
            };

            try {
                fn.call(javaBridge, inviteCode, callbackName);
            } catch (e) {
                delete window[callbackName];
                reject(e);
            }
        });
    },

    /**
     * Crea un nuevo chat de grupo y genera un enlace de invitación.
     * @param chatName Nombre del chat
     * @param maxParticipants Número máximo de participantes
     * @returns Promise con {success, chatId, chatName, inviteCode}
     */
    createNewChat(chatName, maxParticipants) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error("Bridge not ready"));
                return;
            }

            const fn = javaBridge.createNewChat;
            if (typeof fn !== "function") {
                reject(new Error("createNewChat not available"));
                return;
            }

            const callbackName = "_createChatCallback_" + Date.now();
            window[callbackName] = function (responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response && response.success) resolve(response);
                    else reject(new Error((response && (response.message || response.error)) || "Create chat failed"));
                } catch (e) {
                    reject(e);
                }
            };

            try {
                fn.call(javaBridge, chatName, maxParticipants, callbackName);
            } catch (e) {
                delete window[callbackName];
                reject(e);
            }
        });
    }
};

/* Global callbacks */

// Llamado por Java cuando el bridge está listo
window.onBridgeReady = function() {
    Bridge._executeReadyCallbacks();
};

window.onMessageReceived = function (messageJson) {
    try {
        const message = JSON.parse(messageJson);
        if (typeof Chat !== "undefined" && Chat.onMessageReceived) Chat.onMessageReceived(message);
    } catch (e) {
        console.error("Error parsing message:", e);
    }
};

window.onTypingReceived = function (typingJson) {
    try {
        const data = JSON.parse(typingJson);
        if (typeof Chat !== "undefined" && Chat.onTypingReceived) Chat.onTypingReceived(data);
    } catch (e) {
        console.error("Error parsing typing:", e);
    }
};

window.onConnectionStatusChanged = function (connected) {
    const isConnected = connected === "true" || connected === true;
    if (typeof Chat !== "undefined" && Chat.onConnectionStatusChanged) {
        Chat.onConnectionStatusChanged(isConnected);
    }
};


