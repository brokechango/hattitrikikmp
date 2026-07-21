import java.util.Properties
import java.net.URI
import java.util.Base64
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.isFile }
        ?.inputStream()
        ?.use(::load)
}

fun webConfigValue(name: String): String =
    providers.environmentVariable(name).orNull
        ?: providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: ""

fun String.toJavaScriptStringContent(): String = buildString {
    this@toJavaScriptStringContent.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '<', '>', '&', '\u2028', '\u2029' -> append("\\u%04x".format(character.code))
            else -> append(character)
        }
    }
}

fun isSecretSupabaseKey(key: String): Boolean {
    val normalized = key.trim()
    if (normalized.startsWith("sb_secret_", ignoreCase = true)) return true

    val payload = normalized.split('.').getOrNull(1) ?: return false
    val paddedPayload = payload.padEnd((payload.length + 3) / 4 * 4, '=')
    val decodedPayload = runCatching {
        String(Base64.getUrlDecoder().decode(paddedPayload), Charsets.UTF_8)
    }.getOrNull() ?: return false

    return Regex("\"role\"\\s*:\\s*\"service_role\"", RegexOption.IGNORE_CASE)
        .containsMatchIn(decodedPayload)
}

fun validateWebConfig(supabaseUrl: String, publishableKey: String) {
    if (supabaseUrl.isNotBlank()) {
        val uri = runCatching { URI(supabaseUrl) }.getOrNull()
        require(
            uri != null &&
                uri.scheme.equals("https", ignoreCase = true) &&
                !uri.host.isNullOrBlank() &&
                uri.userInfo == null &&
                uri.query == null &&
                uri.fragment == null &&
                (uri.path.isNullOrBlank() || uri.path == "/")
        ) {
            "SUPABASE_URL must be an HTTPS origin without credentials, query parameters or fragments."
        }
    }

    require(!isSecretSupabaseKey(publishableKey)) {
        "SUPABASE_PUBLISHABLE_KEY must never contain a secret or service_role key."
    }
}

val generatedWebConfigDirectory = layout.buildDirectory.dir("generated/webConfig")
val generateWebConfig by tasks.registering(Copy::class) {
    val rawSupabaseUrl = webConfigValue("SUPABASE_URL").trim()
    val rawSupabasePublishableKey = webConfigValue("SUPABASE_PUBLISHABLE_KEY").trim()
    validateWebConfig(rawSupabaseUrl, rawSupabasePublishableKey)

    val supabaseUrl = rawSupabaseUrl.toJavaScriptStringContent()
    val supabasePublishableKey = rawSupabasePublishableKey.toJavaScriptStringContent()

    inputs.property("supabaseUrl", rawSupabaseUrl)
    inputs.property("supabasePublishableKey", rawSupabasePublishableKey)
    from(layout.projectDirectory.file("config/config.js.template")) {
        rename { "config.js" }
        expand(
            "supabaseUrl" to supabaseUrl,
            "supabasePublishableKey" to supabasePublishableKey
        )
    }
    into(generatedWebConfigDirectory)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "hattitriki"
        browser {
            commonWebpackConfig {
                outputFileName = "hattitriki.js"
                sourceMaps = false
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain {
            resources.srcDir(generatedWebConfigDirectory)
            dependencies {
                implementation(projects.shared)
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.supabase.auth)
            }
        }
    }
}

tasks.matching { it.name == "wasmJsProcessResources" }.configureEach {
    dependsOn(generateWebConfig)
}
