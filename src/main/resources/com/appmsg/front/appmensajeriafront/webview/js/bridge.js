/* Bridge */

const Bridge = {
    isReady() {
        return typeof window.javaBridge !== "undefined";
    },

    whenReady(callback) {
        if (this.isReady()) callback();
        else window.onBridgeReady = callback;
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

    log(message) {
        try {
            if (this.isReady() && typeof javaBridge.log === "function") javaBridge.log(message);
        } catch (_) {}
        console.log(message);
    },

    getUserId() {
        if (!this.isReady() || typeof javaBridge.getUserId !== "function") return null;
        try { return javaBridge.getUserId(); } catch (_) { return null; }
    },

    getChatId() {
        if (!this.isReady()) return null;
        const fn = javaBridge.getChatId;
        if (typeof fn !== "function") return null;
        try { return fn.call(javaBridge); } catch (_) { return null; }
    },

    tryToLogin(username, password) {
        if (!this.isReady()) return;
        if (typeof javaBridge.tryToLogin === "function") {
            javaBridge.tryToLogin(username, password);
        }
    },

    navigate(page) {
        if (!this.isReady()) return;

        if (typeof javaBridge.navigate === "function") {
            javaBridge.navigate(page);
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
    }
};

/* Global callbacks */

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

window.addEventListener("load", () => {
    Bridge.whenReady(() => console.log("Bridge listo"));
});
