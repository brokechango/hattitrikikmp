# Hattitriki

Aplicación multiplataforma para llevar los resultados, estadísticas y clasificaciones de una liga de fútbol amistosa.

Construida con Kotlin Multiplatform y Compose Multiplatform, comparte la interfaz y la lógica entre Android, iOS y web. Los resultados y clasificaciones se consultan en Supabase.

> Estado: en desarrollo. Toda la liga requiere una cuenta activa de Supabase; los administradores pueden crear actas completas y dar de alta jugadores.

## Funcionalidades

- Panel de inicio con el último resultado y métricas de la temporada.
- Histórico de partidos y acceso al acta de cada encuentro.
- Clasificaciones de jugadores por estadísticas.
- Detalle de partidos, goles y participación de porteros.
- Login previo a la aplicación para miembros activos de la liga.
- Zona míster visible solo para administradores, con creación de actas completas y altas de jugadores.
- Navegación con animaciones y estado restaurable.
- Interfaz compartida para Android, iOS y navegador mediante Kotlin/Wasm.

## Tecnologías

- [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- Material 3
- Navigation 3 para la navegación compartida
- Kotlinx Serialization y Coroutines
- Ktor y Supabase (dependencias preparadas para la futura capa remota)
- Firebase Analytics, Authentication y Firestore en Android

## Estructura del proyecto

```text
androidApp/  Aplicación Android y configuración de Firebase
iosApp/      Contenedor nativo de iOS en SwiftUI/Xcode
shared/      UI, navegación, modelos, datos y lógica compartida
webApp/      Entrada web, recursos HTML/CSS y configuración Kotlin/Wasm
```

El código común vive principalmente en `shared/src/commonMain`. Las implementaciones específicas de cada plataforma se mantienen en `androidMain` e `iosMain`.

## Requisitos

- JDK 17 o posterior.
- Android Studio actualizado, para ejecutar la app Android.
- Xcode, macOS y un simulador/dispositivo iOS, para ejecutar la app iOS.
- Un proyecto de Firebase si se desea compilar con los servicios Android configurados.

## Configuración local

Antes de compilar Android, añade estos archivos locales; no se versionan para proteger credenciales y claves de firma.

1. Descarga `google-services.json` desde tu proyecto de Firebase y colócalo en `androidApp/google-services.json`.
2. Crea `keystore.properties` en la raíz del repositorio con los datos de tu almacén de claves:

   ```properties
   storeFile=ruta/al/archivo.keystore
   storePassword=tu_contrasena
   keyAlias=tu_alias
   keyPassword=tu_contrasena_de_clave
   ```

3. Comprueba que `local.properties` indique la ruta de tu Android SDK. Android Studio lo crea normalmente de forma automática.
4. Para habilitar el acceso de miembros en Android, añade también estas dos líneas a `local.properties`:

   ```properties
   SUPABASE_URL=https://tu-proyecto.supabase.co
   SUPABASE_PUBLISHABLE_KEY=tu_publishable_key
   ```

   En iOS, crea el archivo local no versionado `iosApp/Configuration/Supabase.xcconfig` con el mismo contenido. Usa la publishable key (o la anon key heredada), nunca `service_role` ni una clave secreta.

   El build web lee esos mismos valores. También se pueden proporcionar mediante las variables de entorno `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY`, o como propiedades Gradle con `-PSUPABASE_URL=...` y `-PSUPABASE_PUBLISHABLE_KEY=...`. El orden de prioridad es: variable de entorno, propiedad Gradle y `local.properties`.

No incluyas estos archivos ni sus valores en commits o incidencias públicas.

### Base de datos de partidos programados

Antes de usar la app, aplica en orden todas las migraciones de `supabase/migrations/`, incluida `20260721120000_private_league_membership.sql`, mediante el SQL Editor o la CLI de Supabase. La última migración exige un perfil activo para consultar resultados, alineaciones, goles y jugadores; las RPC de administración exigen además el rol `admin`. También activa RLS, retira el acceso directo a las tablas y revoca las lecturas a `anon`. El cliente nunca usa una clave `service_role`.

Desactiva el registro público de usuarios en Supabase Auth y crea o invita una cuenta individual para cada miembro. Las cuentas nuevas reciben un perfil inactivo: actívalo como `member` o `admin` con los ejemplos incluidos al final de `20260721120000_private_league_membership.sql`. La autorización real permanece en PostgreSQL; ocultar botones en la interfaz no concede ni sustituye permisos.

### Invitaciones por correo

1. En Supabase, configura **Authentication → URL Configuration → Site URL** con la raíz HTTPS permanente de la web. No uses un túnel temporal. Añade esa misma raíz a **Redirect URLs**.
2. Mantén `{{ .ConfirmationURL }}` en la plantilla **Authentication → Email Templates → Invite user**.
3. Envía la invitación desde **Authentication → Users → Add user → Send invitation**.
4. Activa después el perfil creado por el trigger, eligiendo el rol apropiado:

   ```sql
   update public.profiles
   set role = 'member', is_active = true
   where id = (
       select id
       from auth.users
       where lower(email) = lower('usuario@ejemplo.com')
   );
   ```

Al abrir el enlace, Supabase redirige a la raíz web con una sesión de invitación. Hattitriki la detecta antes de comprobar la membresía, solicita una contraseña de al menos ocho caracteres y la guarda en Auth. El estado pendiente vive solo en `sessionStorage` y se elimina al completar o cancelar el proceso. Si el perfil todavía está inactivo, la contraseña queda configurada, pero la app cierra la sesión hasta que un administrador lo active.

## Ejecutar el proyecto

### Android

Abre el proyecto con Android Studio, selecciona el módulo `androidApp` y ejecuta la configuración de aplicación. También puedes generar un APK de depuración desde la raíz:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

El APK resultante se genera en `androidApp/build/outputs/apk/debug/`.

### iOS

En macOS, abre `iosApp` con Xcode y ejecuta el esquema `iosApp` en un simulador o dispositivo compatible.

### Web

Para arrancar el servidor de desarrollo y abrir Hattitriki en el navegador:

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

Para generar los archivos estáticos optimizados para producción:

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDistribution
```

El bundle se genera bajo `webApp/build/dist/wasmJs/productionExecutable/` y puede desplegarse en cualquier alojamiento de archivos estáticos. La tarea genera `config.js` a partir de la configuración local o del entorno; la publishable key de Supabase está diseñada para clientes públicos, pero nunca debe sustituirse por una clave `service_role` o secreta. El build rechaza claves `sb_secret_` y JWT con rol `service_role`.

La versión web aplica una política CSP estricta, no publica mapas de fuentes y guarda la sesión del miembro en `sessionStorage`: la sesión deja de persistir al cerrar la pestaña. El archivo `_headers` del bundle configura CSP, HSTS, protección frente a iframes, política de permisos y `nosniff` en Netlify, Cloudflare Pages y alojamientos compatibles. Si el proveedor ignora `_headers`, replica esas cabeceras en su configuración; HSTS solo debe activarse en un dominio servido siempre por HTTPS.

Para revisar localmente el bundle con compresión Brotli, cabeceras de caché y una configuración similar a producción:

```powershell
node webApp/preview.mjs
```

La vista previa queda disponible en `http://127.0.0.1:8767`. En el alojamiento definitivo deben conservarse la compresión, las cabeceras de seguridad y las cabeceras de caché para los archivos `.wasm` y JavaScript.

La auditoría Lighthouse debe ejecutarse contra esa build de producción, no contra el servidor de desarrollo:

```powershell
npx lighthouse http://127.0.0.1:8767 --preset=desktop --view
```

También se puede abrir DevTools, elegir Lighthouse, modo `Navigation` y dispositivo `Desktop`.

### CI/CD web con Cloudflare Pages

El workflow `.github/workflows/web-cicd.yml` ejecuta las pruebas y genera la build web en cada pull request dirigido a `main`. Los pushes a `main` y las ejecuciones manuales desde esa rama publican el resultado en Cloudflare Pages mediante Direct Upload. Las credenciales de despliegue no se exponen en los workflows de pull requests.

Antes del primer despliegue:

1. En Cloudflare, crea un proyecto de Pages con **Direct Upload**, configura `main` como rama de producción y anota el nombre del proyecto y el Account ID.
2. Crea un API token de Cloudflare limitado a tu cuenta con el permiso `Account → Cloudflare Pages → Edit`.
3. En `Settings → Secrets and variables → Actions → Secrets`, crea `CLOUDFLARE_API_TOKEN`.
4. En `Settings → Secrets and variables → Actions → Variables`, crea `CLOUDFLARE_ACCOUNT_ID`, `CLOUDFLARE_PAGES_PROJECT`, `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY`. Los valores de Supabase son públicos para el cliente; nunca configures `service_role` ni `sb_secret_`.
5. Crea y protege el entorno de GitHub `cloudflare-pages` para permitir despliegues únicamente desde `main`.
6. Aplica todas las migraciones de Supabase, especialmente `20260721120000_private_league_membership.sql`.

Cloudflare Pages interpreta el archivo `_headers` incluido en el bundle, por lo que el despliegue aplica CSP, HSTS, `nosniff`, protección frente a iframes y la política de permisos configurada por la aplicación.

## Pruebas

Las pruebas de la lógica compartida pueden ejecutarse en el host Android:

```powershell
.\gradlew.bat :shared:testAndroidHostTest
```

En macOS también puedes ejecutar las pruebas del simulador de iOS:

```powershell
./gradlew :shared:iosSimulatorArm64Test
```

## Próximos pasos

- Añadir actualización manual o en tiempo real para reflejar nuevos resultados sin reiniciar la pantalla.

## Licencia

Este repositorio no incluye una licencia explícita. Todos los derechos quedan reservados hasta que se añada una licencia.
