const pages = {
    home:     { html: 'home.html',     js: 'js/home.js' },
    chat:     { html: 'chat.html',     js: 'js/chat.js' },
    profile:  { html: 'profile.html',  js: 'js/profile.js' },
    invite:   { html: 'invite.html',   js: 'js/invite.js' },
    settings: { html: 'settings.html', js: 'js/settings.js' }
};

let history = [];
let currentPage = null;

function fetchText(url) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', url, true);
        xhr.onreadystatechange = function () {
            if (xhr.readyState !== 4) return;
            if (xhr.status >= 200 && xhr.status < 300) resolve(xhr.responseText);
            else reject(new Error('HTTP ' + xhr.status + ' al cargar ' + url));
        };
        xhr.onerror = () => reject(new Error('Network error al cargar ' + url));
        xhr.send();
    });
}

async function loadPage(pageName) {
    const resolvedName = pages[pageName] ? pageName : 'home';
    const page = pages[resolvedName];

    try {
        const html = await fetchText(page.html);
        document.getElementById('app').innerHTML = html;

        await loadScript(page.js);

        if (currentPage && currentPage !== resolvedName) history.push(currentPage);
        currentPage = resolvedName;

    } catch (e) {
        console.error(e);
        document.getElementById('app').innerHTML = `
        <div class="content" style="display:grid; place-items:center; min-height:60vh;">
          <div class="card" style="max-width:520px;">
            <h3 class="card-title">Error</h3>
            <p class="muted">No se ha podido cargar la página.</p>
            <p class="muted" style="margin-top:8px; font-size:12px;">${String(e.message || e)}</p>
            <button class="btn btn-secondary" onclick="loadPage('home')">Volver a inicio</button>
          </div>
        </div>
      `;
    }
}

function loadScript(src) {
    return new Promise((resolve, reject) => {
        const existing = document.querySelector(`script[data-page-script="${src}"]`);
        if (existing) existing.remove();

        const script = document.createElement('script');
        script.src = src;
        script.setAttribute('data-page-script', src);
        script.onload = resolve;
        script.onerror = () => reject(new Error('No se pudo cargar el script: ' + src));
        document.body.appendChild(script);
    });
}

function goBack() {
    if (history.length > 0) {
        const prev = history.pop();
        currentPage = null;
        loadPage(prev);
    } else {
        loadPage('home');
    }
}

function onBridgeReady() {
    const params = (typeof Bridge !== 'undefined' && Bridge.getInitParams) ? Bridge.getInitParams() : {};
    const initialPage = params.page || 'home';

    if (params.theme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
    }

    loadPage(initialPage);
}

// Si Java inyecta el bridge después, WebViewManager llamará a onBridgeReady().
// Si ya estuviera inyectado, lo lanzamos nosotros.
if (typeof javaBridge !== 'undefined') onBridgeReady();