const Settings = {
    init: function () {
        const wp = document.getElementById("wallpaper");
        // Obtener wallpapers desde Java (URLs absolutas)
        let wallpapers = [];
        if (window.javaBridge && typeof javaBridge.getWallpapersJson === "function") {
            try {
                wallpapers = JSON.parse(javaBridge.getWallpapersJson());
            } catch (e) { wallpapers = []; }
        }
        wallpapers.forEach(v => {
            const opt = document.createElement("option");
            opt.value = v;
            opt.textContent = v.split("/").pop();
            wp.appendChild(opt);
        });

        // Preview inicial
        this.updateWallpaperPreview(wp.value);
        wp.addEventListener("change", (e) => {
            this.updateWallpaperPreview(e.target.value);
        });

        document.getElementById("darkMode").addEventListener("change", (e) => {
            document.documentElement.dataset.theme = e.target.checked ? "dark" : "light";
            if (window.javaBridge && typeof javaBridge.setDarkMode === "function") javaBridge.setDarkMode(e.target.checked);
        });

        this.setStatus("Cargando ajustes...");
        if (window.javaBridge && typeof javaBridge.loadSettings === "function") javaBridge.loadSettings();
    },

    updateWallpaperPreview: function (imgPath) {
        const preview = document.getElementById("wallpaperPreview");
        if (!imgPath) {
            preview.innerHTML = "<span class='muted'>Sin fondo seleccionado</span>";
            return;
        }
        preview.innerHTML = `<img src="${imgPath}" alt="Preview" style="max-width:320px;max-height:180px;border-radius:8px;box-shadow:0 2px 8px #0002;">`;
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
        if (window.javaBridge && typeof javaBridge.saveSettings === "function") javaBridge.saveSettings(JSON.stringify(dto));
    },

    chooseWallpaper: function () {
        // Abre el FileChooser de Java
        if (window.javaBridge && typeof javaBridge.chooseWallpaper === "function") {
            javaBridge.chooseWallpaper();
        } else {
            this.setStatus("No implementado: elegir archivo");
        }
    }
}

// Recibe la imagen elegida desde Java y la añade como opción seleccionada
function onWallpaperChosen(obj) {
    if (!obj || !obj.uri) return;
    const wp = document.getElementById("wallpaper");
    // Si ya existe, selecciona; si no, añade y selecciona
    let found = false;
    for (let i = 0; i < wp.options.length; i++) {
        if (wp.options[i].value === obj.uri) {
            wp.selectedIndex = i;
            found = true;
            break;
        }
    }
    if (!found) {
        const opt = document.createElement("option");
        opt.value = obj.uri;
        opt.textContent = obj.uri.split("/").pop();
        wp.appendChild(opt);
        wp.value = obj.uri;
    }
    Settings.updateWallpaperPreview(obj.uri);
    Settings.setStatus("Fondo personalizado seleccionado");
}

// callbacks desde Java
function onSettingsLoaded(dto) {
    if (!dto) {
        Settings.setStatus("No hay ajustes guardados aún. Puedes crear los tuyos.");
        return;
    }
    document.getElementById("darkMode").checked = !!dto.darkMode;
    const wp = document.getElementById("wallpaper");
    // Si el fondo guardado no está en el select, lo añade
    if (dto.wallpaperPath && dto.wallpaperPath !== "") {
        let found = false;
        for (let i = 0; i < wp.options.length; i++) {
            if (wp.options[i].value === dto.wallpaperPath) {
                found = true;
                break;
            }
        }
        if (!found) {
            const opt = document.createElement("option");
            opt.value = dto.wallpaperPath;
            opt.textContent = dto.wallpaperPath.split("/").pop();
            wp.appendChild(opt);
        }
        wp.value = dto.wallpaperPath;
    } else {
        wp.value = "";
    }
    // Forzar actualización de preview
    Settings.updateWallpaperPreview(wp.value);
    document.getElementById("displayName").value = dto.displayName || "";
    document.getElementById("status").value = dto.status || "";
    // Aplica el tema global
    Utils.applyTheme(!!dto.darkMode);
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

Bridge.whenReady(() => Settings.init());
