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
        if (
            path.startsWith("http://") ||
            path.startsWith("https://") ||
            path.startsWith("data:") ||
            path.startsWith("file:") ||
            path.startsWith("blob:")
        ) return path;
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
        if (typeof javaBridge.verifyRegister === "function") {
            javaBridge.verifyRegister(code);
        }
    },
    resendVerificationCode(email) {
        if (!this.isReady()) return;
        if (typeof javaBridge.resendVerificationCode === "function") {
            javaBridge.resendVerificationCode(email);
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

    loadMessages(chatId) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) {
                reject(new Error("Bridge not ready"));
                return;
            }

            const fn = javaBridge.loadMessages;
            if (typeof fn !== "function") {
                reject(new Error("loadMessages not available"));
                return;
            }

            const callbackName = "_messagesCallback_" + Date.now();
            window[callbackName] = function (responseJson) {
                delete window[callbackName];
                try {
                    const response = JSON.parse(responseJson);
                    if (response.success === false) {
                        reject(new Error(response.error || "Failed to load messages"));
                    } else {
                        // Si la respuesta es directamente un array, la devolvemos
                        resolve(Array.isArray(response) ? response : (response.messages || []));
                    }
                } catch (e) {
                    reject(e);
                }
            };

            try {
                fn.call(javaBridge, chatId, callbackName);
            } catch (e) {
                delete window[callbackName];
                reject(e);
            }
        });
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
     * @param imageUrl URL de la imagen del chat (opcional)
     * @returns Promise con {success, chatId, chatName, inviteCode}
     */
    createNewChat(chatName, maxParticipants, imageUrl) {
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
                fn.call(javaBridge, chatName, maxParticipants, imageUrl || "", callbackName);
            } catch (e) {
                delete window[callbackName];
                reject(e);
            }
        });
    },

    deleteChat(chatId) {
            return new Promise((resolve, reject) => {
                if (!this.isReady()) {
                    reject(new Error("Bridge not ready"));
                    return;
                }

                const fn = javaBridge.deleteChat;
                if (typeof fn !== "function") {
                    reject(new Error("deleteChat not available"));
                    return;
                }

                const callbackName = "_deleteChatCallback_" + Date.now();
                window[callbackName] = function (responseJson) {
                    delete window[callbackName];
                    try {
                        const response = JSON.parse(responseJson);
                        if (response && response.success) resolve(response);
                        else reject(new Error((response && (response.message || response.error)) || "Delete chat failed"));
                    } catch (e) {
                        reject(e);
                    }
                };

                try {
                    fn.call(javaBridge, chatId || "", callbackName);
                } catch (e) {
                    delete window[callbackName];
                    reject(e);
                }
            });
        },

    getChatInfo(chatId) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) { reject(new Error("Bridge not ready")); return; }
            const fn = javaBridge.getChatInfo;
            if (typeof fn !== "function") { reject(new Error("getChatInfo not available")); return; }
            const cb = "_chatInfoCb_" + Date.now();
            window[cb] = function(data) {
                delete window[cb];
                if (data && data.error) reject(new Error(data.error));
                else resolve(data);
            };
            try { fn.call(javaBridge, chatId, cb); } catch(e) { delete window[cb]; reject(e); }
        });
    },

    updateChatInfo(chatId, updates) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) { reject(new Error("Bridge not ready")); return; }
            const fn = javaBridge.updateChatInfo;
            if (typeof fn !== "function") { reject(new Error("updateChatInfo not available")); return; }
            const cb = "_updateChatCb_" + Date.now();
            window[cb] = function(data) {
                delete window[cb];
                if (data && data.success) resolve(data);
                else reject(new Error((data && data.error) || "Update failed"));
            };
            try {
                const body = JSON.stringify(Object.assign({ chatId }, updates));
                fn.call(javaBridge, body, cb);
            } catch(e) { delete window[cb]; reject(e); }
        });
    },

    addMemberToChat(chatId, username) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) { reject(new Error("Bridge not ready")); return; }
            const fn = javaBridge.addMemberToChat;
            if (typeof fn !== "function") { reject(new Error("addMemberToChat not available")); return; }
            const cb = "_addMemberCb_" + Date.now();
            window[cb] = function(data) {
                delete window[cb];
                if (data && data.success) resolve(data);
                else reject(new Error((data && data.error) || "Add member failed"));
            };
            try { fn.call(javaBridge, chatId, username, cb); } catch(e) { delete window[cb]; reject(e); }
        });
    },

    getChatMedia(chatId) {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) { reject(new Error("Bridge not ready")); return; }
            const fn = javaBridge.getChatMedia;
            if (typeof fn !== "function") { resolve([]); return; }
            const cb = "_chatMediaCb_" + Date.now();
            window[cb] = function(data) {
                delete window[cb];
                resolve(Array.isArray(data) ? data : []);
            };
            try { fn.call(javaBridge, chatId, cb); } catch(e) { delete window[cb]; resolve([]); }
        });
    },

    chooseChatImage() {
        return new Promise((resolve, reject) => {
            if (!this.isReady()) { reject(new Error("Bridge not ready")); return; }
            const fn = javaBridge.chooseChatImage;
            if (typeof fn !== "function") { reject(new Error("chooseChatImage not available")); return; }
            const cb = "_chooseChatImgCb_" + Date.now();
            window[cb] = function(data) {
                delete window[cb];
                if (data && data.success && data.files) {
                    try {
                        const urls = JSON.parse(data.files);
                        resolve(Array.isArray(urls) && urls.length > 0 ? urls[0] : null);
                    } catch(e) { resolve(null); }
                } else {
                    reject(new Error((data && data.error) || "Upload failed"));
                }
            };
            try { fn.call(javaBridge, cb); } catch(e) { delete window[cb]; reject(e); }
        });
    },
    setLoading: function(isLoading, button) {
        if (isLoading) {
            document.body.classList.add("loading-cursor");
            button.disabled = true;
            button.style.opacity = "0.6";
        } else {
            document.body.classList.remove("loading-cursor");
            button.disabled = false;
            button.style.opacity = "";
        }
    },
    getContacts() {
        if (!this.isReady()) return;

        if (typeof javaBridge.getContacts === "function") {
            javaBridge.getContacts();
        }
    },

    addContact(identifier) {
        if (!this.isReady()) return;

        if (typeof javaBridge.addContact === "function") {
            javaBridge.addContact(identifier);
        }
    },

    removeContact(contactId) {
        if (!this.isReady()) return;

        if (typeof javaBridge.removeContact === "function") {
            javaBridge.removeContact(contactId);
        }
    },
};

Bridge.openPrivateChat = function(contactId) {
    if (javaBridge && javaBridge.openPrivateChat) {
        javaBridge.openPrivateChat(contactId);
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


