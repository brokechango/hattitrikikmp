import { createServer } from "node:http";
import { constants as zlibConstants, brotliCompress } from "node:zlib";
import { promisify } from "node:util";
import { extname, isAbsolute, join, normalize, relative, resolve } from "node:path";
import { readFile, realpath, stat } from "node:fs/promises";

const compress = promisify(brotliCompress);
const defaultRoot = join(
    import.meta.dirname,
    "build",
    "dist",
    "wasmJs",
    "productionExecutable"
);
const root = resolve(process.argv[2] ?? defaultRoot);
const canonicalRoot = await realpath(root);
const port = Number.parseInt(process.env.PORT ?? "8767", 10);
const compressedFiles = new Map();

const contentSecurityPolicy = [
    "default-src 'self'",
    "base-uri 'none'",
    "object-src 'none'",
    "frame-ancestors 'none'",
    "form-action 'self'",
    "script-src 'self' 'wasm-unsafe-eval'",
    "style-src 'self'",
    "img-src 'self' data: blob:",
    "font-src 'self' data:",
    "connect-src 'self' https://*.supabase.co wss://*.supabase.co",
    "worker-src 'self' blob:",
    "manifest-src 'self'",
    "media-src 'self'"
].join("; ");

const contentTypes = {
    ".css": "text/css; charset=utf-8",
    ".html": "text/html; charset=utf-8",
    ".js": "text/javascript; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".map": "application/json; charset=utf-8",
    ".mjs": "text/javascript; charset=utf-8",
    ".png": "image/png",
    ".svg": "image/svg+xml; charset=utf-8",
    ".txt": "text/plain; charset=utf-8",
    ".wasm": "application/wasm"
};

const compressibleExtensions = new Set([
    ".css",
    ".html",
    ".js",
    ".json",
    ".map",
    ".mjs",
    ".svg",
    ".wasm"
]);

function cacheControl(fileName) {
    if (fileName === "index.html" || fileName === "config.js") {
        return "no-store";
    }
    if (fileName === "hattitriki.js" || fileName === "bootstrap.js" || fileName === "app.css") {
        return "no-cache";
    }
    if (/^[a-f0-9]{20}\.wasm$/.test(fileName)) {
        return "public, max-age=31536000, immutable";
    }
    return "public, max-age=2592000";
}

function safePath(requestPath) {
    let decodedPath;
    try {
        decodedPath = decodeURIComponent(requestPath.split("?")[0]);
    } catch {
        return null;
    }
    if (decodedPath.includes("\0")) return null;

    const relativePath = normalize(decodedPath).replace(/^[/\\]+/, "") || "index.html";
    const candidate = resolve(root, relativePath);
    const outsideRoot = relative(root, candidate).startsWith("..") || isAbsolute(relative(root, candidate));
    return outsideRoot ? null : candidate;
}

function setSecurityHeaders(response) {
    response.setHeader("Content-Security-Policy", contentSecurityPolicy);
    response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
    response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
    response.setHeader("Origin-Agent-Cluster", "?1");
    response.setHeader(
        "Permissions-Policy",
        "accelerometer=(), ambient-light-sensor=(), autoplay=(), battery=(), camera=(), " +
            "display-capture=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), " +
            "payment=(), usb=()"
    );
    response.setHeader("Referrer-Policy", "no-referrer");
    response.setHeader("Strict-Transport-Security", "max-age=63072000; includeSubDomains");
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
}

async function readCompressed(filePath, content) {
    if (!compressedFiles.has(filePath)) {
        compressedFiles.set(
            filePath,
            compress(content, {
                params: {
                    [zlibConstants.BROTLI_PARAM_QUALITY]: 6
                }
            })
        );
    }
    return compressedFiles.get(filePath);
}

const server = createServer(async (request, response) => {
    setSecurityHeaders(response);

    try {
        if (request.method !== "GET" && request.method !== "HEAD") {
            response.setHeader("Allow", "GET, HEAD");
            response.writeHead(405, { "Content-Type": "text/plain; charset=utf-8" });
            response.end("Method not allowed");
            return;
        }

        let filePath = safePath(request.url ?? "/");
        if (!filePath) {
            response.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" });
            response.end("Bad request");
            return;
        }

        const fileStat = await stat(filePath).catch(() => null);
        if (fileStat?.isDirectory()) {
            filePath = join(filePath, "index.html");
        }

        filePath = await realpath(filePath);
        const canonicalRelativePath = relative(canonicalRoot, filePath);
        if (canonicalRelativePath.startsWith("..") || isAbsolute(canonicalRelativePath)) {
            response.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" });
            response.end("Bad request");
            return;
        }

        const content = await readFile(filePath);
        const extension = extname(filePath).toLowerCase();
        const acceptsBrotli = request.headers["accept-encoding"]?.includes("br") === true;
        const body = acceptsBrotli && compressibleExtensions.has(extension)
            ? await readCompressed(filePath, content)
            : content;

        response.setHeader("Content-Type", contentTypes[extension] ?? "application/octet-stream");
        response.setHeader("Content-Length", body.length);
        response.setHeader("Cache-Control", cacheControl(filePath.split(/[\\/]/).at(-1)));
        if (body !== content) {
            response.setHeader("Content-Encoding", "br");
            response.setHeader("Vary", "Accept-Encoding");
        }

        response.writeHead(200);
        response.end(request.method === "HEAD" ? undefined : body);
    } catch (error) {
        const status = error?.code === "ENOENT" ? 404 : 500;
        response.writeHead(status, { "Content-Type": "text/plain; charset=utf-8" });
        response.end(status === 404 ? "Not found" : "Internal server error");
    }
});

server.listen(port, "127.0.0.1", () => {
    console.log(`Hattitriki production preview: http://127.0.0.1:${port}`);
    console.log(`Serving: ${root}`);
});
