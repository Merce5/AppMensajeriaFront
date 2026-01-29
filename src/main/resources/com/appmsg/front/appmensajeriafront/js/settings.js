const Settings = {
    init: function () {
        // rellenar wallpapers igual que hacías en JavaFX
        const wp = document.getElementById("wallpaper");
        [
            "/com/appmsg/front/appmensajeriafront/wallpapers/wp1.jpg",
            "/com/appmsg/front/appmensajeriafront/wallpapers/wp2.jpg",
            "/com/appmsg/front/appmensajeriafront/wallpapers/wp3.jpg"
        ].forEach(v => {
            const opt = document.createElement("option");
            opt.value = v;
            opt.textContent = v.split("/").pop();
            wp.appendChild(opt);
        });

        document.getElementById("darkMode").addEventListener("change", (e) => {
            // opcional: tema HTML
            document.documentElement.dataset.theme = e.target.checked ? "dark" : "light";
            // y/o notificar a Java
            if (window.settingsBridge) settingsBridge.setDarkMode(e.target.checked);
        });

        Bridge.whenReady(() => {
            this.setStatus("Cargando ajustes...");
            settingsBridge.loadSettings();
        });
    },

    setStatus: function (msg) {
        document.getElementById("statusLabel").textContent = msg || "";
    },

    save: function () {
        const dto = {
            darkMode: document.getElementById("darkMode").checked,
            wallpaperPath: document.getElementById("wallpaper").value,
            displayName: document.getElementById("displayName").value,
            status: document.getElementById("status").value
        };
        this.setStatus("Guardando...");
        settingsBridge.saveSettings(JSON.stringify(dto));
    },

    chooseWallpaper: function () {
        // esto tiene que abrirlo Java con FileChooser
        // settingsBridge.chooseWallpaper() -> Java abre FileChooser -> JS recibe onWallpaperChosen(uri)
        if (window.settingsBridge && settingsBridge.chooseWallpaper) {
            settingsBridge.chooseWallpaper();
        } else {
            this.setStatus("No implementado: elegir archivo");
        }
    }
};

// callbacks desde Java
function onSettingsLoaded(dto) {
    if (!dto) {
        Settings.setStatus("No hay ajustes guardados aún. Puedes crear los tuyos.");
        return;
    }
    document.getElementById("darkMode").checked = !!dto.darkMode;
    document.getElementById("wallpaper").value = dto.wallpaperPath || "";
    document.getElementById("displayName").value = dto.displayName || "";
    document.getElementById("status").value = dto.status || "";
    Settings.setStatus("Ajustes cargados.");
}

function onSettingsSaved(saved) {
    Settings.setStatus("Guardado.");
}

function onSettingsError(errJsonString) {
    // si lo mandas como string
    try {
        const err = JSON.parse(errJsonString);
        Settings.setStatus("Error: " + (err.message || "desconocido"));
    } catch {
        Settings.setStatus("Error: " + errJsonString);
    }
}

window.addEventListener("load", () => Settings.init());
