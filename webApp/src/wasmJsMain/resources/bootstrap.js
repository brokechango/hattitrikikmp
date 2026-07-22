"use strict";

(() => {
    const observedCanvases = new WeakSet();
    const loadingScreen = document.getElementById("app-loading");
    const composeRoot = document.getElementById("composeApp");
    const loginForm = document.getElementById("bootstrap-login");
    const emailInput = document.getElementById("bootstrap-email");
    const passwordInput = document.getElementById("bootstrap-password");
    const submitButton = document.getElementById("bootstrap-submit");
    const forgotPasswordButton = document.getElementById("bootstrap-forgot-password");
    const authCallbackMessage = document.getElementById("auth-callback-message");
    const status = document.querySelector(".loading-status");
    let retryTimer = 0;
    let retryDelay = 32;
    let scanQueued = false;
    let readyScheduled = false;
    let appStarted = false;
    let mountRequested = false;
    let submitLogin = false;
    let appLoadTimer = 0;
    const mountCallbacks = [];

    const makeCanvasAccessible = (canvas) => {
        if (canvas.getAttribute("role") === "generic") {
            canvas.removeAttribute("role");
        }

        if (!observedCanvases.has(canvas)) {
            observedCanvases.add(canvas);
            new MutationObserver(() => {
                if (canvas.getAttribute("role") === "generic") {
                    canvas.removeAttribute("role");
                }
            }).observe(canvas, {
                attributes: true,
                attributeFilter: ["role"]
            });
        }
    };

    const sanitize = (root) => {
        let canvasFound = false;

        root.querySelectorAll?.("canvas").forEach((canvas) => {
            makeCanvasAccessible(canvas);
            canvasFound = true;
        });

        root.querySelectorAll?.("*").forEach((element) => {
            if (element.shadowRoot) {
                canvasFound = sanitize(element.shadowRoot) || canvasFound;
            }
        });

        return canvasFound;
    };

    const markReadyAfterPaint = () => {
        if (readyScheduled) return;
        readyScheduled = true;
        observer.disconnect();
        window.clearTimeout(retryTimer);

        window.requestAnimationFrame(() => {
            window.requestAnimationFrame(() => {
                document.documentElement.dataset.composeReady = "true";
                loadingScreen?.setAttribute("aria-busy", "false");
            });
        });
    };

    const scan = () => {
        scanQueued = false;
        retryTimer = 0;

        if (sanitize(document)) {
            markReadyAfterPaint();
            return;
        }

        retryTimer = window.setTimeout(requestScan, retryDelay);
        retryDelay = Math.min(500, Math.ceil(retryDelay * 1.6));
    };

    function requestScan() {
        if (scanQueued || readyScheduled) return;
        scanQueued = true;
        window.queueMicrotask(scan);
    }

    const observer = new MutationObserver(requestScan);
    if (composeRoot) {
        observer.observe(composeRoot, { childList: true, subtree: true });
    }

    const updateSubmitButton = () => {
        if (!submitButton) return;
        submitButton.disabled = !emailInput?.value.trim() || !passwordInput?.value;
    };

    const loadApp = () => {
        window.clearTimeout(appLoadTimer);
        appLoadTimer = 0;
        if (appStarted) return;
        appStarted = true;

        const appScript = document.createElement("script");
        appScript.id = "app-script";
        appScript.src = "hattitriki.js";
        appScript.fetchPriority = "high";
        appScript.referrerPolicy = "no-referrer";
        appScript.addEventListener("error", () => {
            appStarted = false;
            appScript.remove();
            loadingScreen?.setAttribute("aria-busy", "false");
            if (submitButton) {
                submitButton.textContent = "Reintentar";
                updateSubmitButton();
            }
            if (status) status.textContent = "No se pudo cargar la aplicación.";
        }, { once: true });
        document.head.append(appScript);
    };

    const scheduleAppLoad = () => {
        window.clearTimeout(appLoadTimer);
        appLoadTimer = window.setTimeout(loadApp, 600);
    };

    const requestMount = (withLogin) => {
        submitLogin = withLogin;
        if (mountRequested) return;

        mountRequested = true;
        loadingScreen?.setAttribute("aria-busy", "true");
        if (status) status.textContent = "Cargando la aplicación…";
        requestScan();

        mountCallbacks.splice(0).forEach((callback) => {
            window.queueMicrotask(callback);
        });
    };

    const bridge = {
        get email() {
            return emailInput?.value ?? "";
        },
        get password() {
            return passwordInput?.value ?? "";
        },
        get submitLogin() {
            return submitLogin;
        },
        whenMountRequested(callback) {
            if (mountRequested) {
                window.queueMicrotask(callback);
            } else {
                mountCallbacks.push(callback);
            }
        },
        clearCredentials() {
            if (passwordInput) passwordInput.value = "";
            submitLogin = false;
        }
    };
    Object.defineProperty(globalThis, "HATTITRIKI_BOOTSTRAP", {
        value: bridge,
        configurable: true
    });

    loginForm?.addEventListener("input", () => {
        updateSubmitButton();
        scheduleAppLoad();
    });
    loginForm?.addEventListener("submit", (event) => {
        event.preventDefault();
        if (!loginForm.checkValidity()) {
            loginForm.reportValidity();
            return;
        }

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = "Entrando…";
        }
        requestMount(true);
        loadApp();
    });
    forgotPasswordButton?.addEventListener("click", () => {
        requestMount(false);
        loadApp();
    });
    updateSubmitButton();

    const hasSavedSession = (() => {
        try {
            return globalThis.sessionStorage?.getItem("hattitriki-session") != null;
        } catch (_) {
            return false;
        }
    })();
    const authCallbackType = (() => {
        try {
            const hash = globalThis.location?.hash?.replace(/^#/, "") ?? "";
            return new URLSearchParams(hash).get("type")
                ?? new URLSearchParams(globalThis.location?.search ?? "").get("type")
                ?? "";
        } catch (_) {
            return "";
        }
    })();

    const isAuthCallback = authCallbackType === "invite" || authCallbackType === "recovery";
    if (isAuthCallback) {
        document.documentElement.dataset.authCallback = authCallbackType;
        if (authCallbackMessage) {
            authCallbackMessage.hidden = false;
            authCallbackMessage.textContent = authCallbackType === "invite"
                ? "Estamos preparando tu invitación…"
                : "Estamos preparando la recuperación de tu contraseña…";
        }
    }

    if (hasSavedSession || isAuthCallback) {
        requestMount(false);
        if (isAuthCallback) {
            scheduleAppLoad();
        } else {
            loadApp();
        }
    }
})();
