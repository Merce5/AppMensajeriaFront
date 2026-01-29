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
    tryToLogin: function(username, password) {
        if (!this.isReady()) {
            return;
        }
        javaBridge.tryToLogin(username, password);
    },

    // ==================== LOG ====================

    log: function(message) {
        if (this.isReady()) {
            javaBridge.log(message);
        }
        console.log(message);
    }
};

window.onload = function() {
    Bridge.whenReady(function() {
        console.log("Bridge listo");
    });
};