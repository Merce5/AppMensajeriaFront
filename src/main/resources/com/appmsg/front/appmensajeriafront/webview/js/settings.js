const Settings = {
    selectedPicturePath: "",

    describeSelection: function (path, fallbackText) {
        if (!path) return fallbackText;
        const cleanPath = path.split(/[\\/]/).pop() || path;
        return cleanPath.length > 40 ? cleanPath.slice(0, 18) + "..." + cleanPath.slice(-16) : cleanPath;
    },

    init: function () {
        const theme = document.getElementById("themeKey");
        const wp = document.getElementById("wallpaper");

        Utils.getThemeOptions().forEach(item => {
            const opt = document.createElement("option");
            opt.value = item.value;
            opt.textContent = item.label;
            theme.appendChild(opt);
        });

        let wallpapers = [];
        if (window.javaBridge && typeof javaBridge.getWallpapersJson === "function") {
            try {
                wallpapers = JSON.parse(javaBridge.getWallpapersJson());
            } catch (e) {
                wallpapers = [];
            }
        }

        wallpapers.forEach(v => {
            const opt = document.createElement("option");
            opt.value = v;
            opt.textContent = v.split("/").pop();
            wp.appendChild(opt);
        });

        this.updateWallpaperPreview(wp.value);
        this.updateProfilePicturePreview("");

        wp.addEventListener("change", (e) => {
            this.updateWallpaperPreview(e.target.value);
        });

        theme.addEventListener("change", (e) => {
            const selected = Utils.themeLibrary[e.target.value] || Utils.themeLibrary.classic;
            document.getElementById("darkMode").checked = !!selected.darkMode;
            Utils.applyTheme({ darkMode: !!selected.darkMode, themeKey: e.target.value });
        });

        document.getElementById("darkMode").addEventListener("change", (e) => {
            Utils.applyTheme({
                darkMode: e.target.checked,
                themeKey: document.getElementById("themeKey").value || "classic"
            });
        });

        this.setStatus("Cargando ajustes...");
        if (window.javaBridge && typeof javaBridge.loadSettings === "function") {
            javaBridge.loadSettings();
        }
    },

    updateWallpaperPreview: function (imgPath) {
        const preview = document.getElementById("wallpaperPreview");
        const meta = document.getElementById("wallpaperMeta");
        if (!imgPath) {
            preview.innerHTML = "<span class='muted'>Sin fondo seleccionado</span>";
            meta.textContent = "Sin fondo seleccionado";
            return;
        }
        meta.textContent = this.describeSelection(imgPath, "Usando fondo predefinido");
        preview.innerHTML = `<img src="${imgPath}" alt="Preview" style="max-width:320px;max-height:180px;border-radius:8px;box-shadow:0 2px 8px #0002;">`;
    },

    updateProfilePicturePreview: function (imgPath) {
        const preview = document.getElementById("profilePicturePreview");
        const meta = document.getElementById("profilePictureMeta");
        preview.src = imgPath || Utils.getDefaultAvatar();
        meta.textContent = this.describeSelection(imgPath, "Usando avatar actual");
    },

    setStatus: function (msg) {
        document.getElementById("statusLabel").textContent = msg || "";
    },

    save: function () {
        const dto = {
            darkMode: document.getElementById("darkMode").checked,
            themeKey: document.getElementById("themeKey").value,
            wallpaperPath: document.getElementById("wallpaper").value,
            displayName: document.getElementById("displayName").value,
            status: document.getElementById("status").value,
            picturePath: this.selectedPicturePath
        };
        this.setStatus("Guardando...");
        if (window.javaBridge && typeof javaBridge.saveSettings === "function") {
            javaBridge.saveSettings(JSON.stringify(dto));
        }
    },

    chooseWallpaper: function () {
        if (window.javaBridge && typeof javaBridge.chooseWallpaper === "function") {
            javaBridge.chooseWallpaper();
        } else {
            this.setStatus("No implementado: elegir archivo");
        }
    },

    chooseProfilePicture: function () {
        if (window.javaBridge && typeof javaBridge.chooseProfilePicture === "function") {
            javaBridge.chooseProfilePicture();
        } else {
            this.setStatus("No implementado: elegir foto de perfil");
        }
    }
};

function onWallpaperChosen(obj) {
    if (!obj || !obj.uri) return;
    const wp = document.getElementById("wallpaper");
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

function onProfilePictureChosen(obj) {
    if (!obj || !obj.uri) return;
    Settings.selectedPicturePath = obj.uri;
    Settings.updateProfilePicturePreview(obj.uri);
    Settings.setStatus("Foto de perfil seleccionada");
}

function onSettingsLoaded(dto) {
    if (!dto) {
        Settings.setStatus("No hay ajustes guardados aun. Puedes crear los tuyos.");
        return;
    }

    document.getElementById("darkMode").checked = !!dto.darkMode;
    document.getElementById("themeKey").value = dto.themeKey || (dto.darkMode ? "dark" : "classic");

    const wp = document.getElementById("wallpaper");
    if (dto.wallpaperPath) {
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

    Settings.updateWallpaperPreview(wp.value);
    document.getElementById("displayName").value = dto.displayName || "";
    document.getElementById("status").value = dto.status || "";
    Settings.selectedPicturePath = dto.picturePath || "";
    Settings.updateProfilePicturePreview(Settings.selectedPicturePath);
    Utils.applyTheme(dto);
    Settings.setStatus(dto.firstLogin ? "Primer acceso: revisa y guarda tu perfil." : "Ajustes cargados.");
}

function onSettingsSaved(saved) {
    if (saved) {
        Settings.selectedPicturePath = saved.picturePath || Settings.selectedPicturePath;
        Settings.updateProfilePicturePreview(Settings.selectedPicturePath);
    }
    Settings.setStatus("Guardado.");
}

function onSettingsError(errJsonString) {
    try {
        const err = JSON.parse(errJsonString);
        Settings.setStatus("Error: " + (err.message || "desconocido"));
    } catch {
        Settings.setStatus("Error: " + errJsonString);
    }
}

Bridge.whenReady(() => Settings.init());
