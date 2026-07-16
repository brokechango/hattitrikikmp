# Hattitriki

Aplicación multiplataforma para llevar los resultados, estadísticas y clasificaciones de una liga de fútbol amistosa.

Construida con Kotlin Multiplatform y Compose Multiplatform, comparte la interfaz y la lógica entre Android e iOS. Los resultados y clasificaciones se consultan en Supabase.

> Estado: en desarrollo. La consulta pública y la zona de administración usan Supabase; los administradores pueden crear actas completas y dar de alta jugadores.

## Funcionalidades

- Panel de inicio con el último resultado y métricas de la temporada.
- Histórico de partidos y acceso al acta de cada encuentro.
- Clasificaciones de jugadores por estadísticas.
- Detalle de partidos, goles y participación de porteros.
- Zona míster con login de Supabase, creación de actas completas y altas de jugadores para administradores.
- Navegación con animaciones y estado restaurable.
- Interfaz compartida para Android e iOS.

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
4. Para habilitar el login de administrador en Android, añade también estas dos líneas a `local.properties`:

   ```properties
   SUPABASE_URL=https://tu-proyecto.supabase.co
   SUPABASE_PUBLISHABLE_KEY=tu_publishable_key
   ```

   En iOS, crea el archivo local no versionado `iosApp/Configuration/Supabase.xcconfig` con el mismo contenido. Usa la publishable key (o la anon key heredada), nunca `service_role` ni una clave secreta.

No incluyas estos archivos ni sus valores en commits o incidencias públicas.

### Base de datos de partidos programados

Antes de usar la app, aplica en orden `supabase/migrations/20260716140000_admin_acta_functions.sql`, `supabase/migrations/20260716150000_admin_edit_delete_functions.sql`, `supabase/migrations/20260716160000_multiple_goalkeepers_per_match.sql`, `supabase/migrations/20260716170000_own_goals.sql`, `supabase/migrations/20260716180000_players_can_participate_in_both_teams.sql`, `supabase/migrations/20260716190000_fix_own_goal_team_validation.sql`, `supabase/migrations/20260716200000_penalty_shootouts.sql`, `supabase/migrations/20260716210000_cast_acta_team_side.sql` y `supabase/migrations/20260716220000_public_league_read_functions.sql` en el SQL Editor de tu proyecto Supabase o con la CLI de Supabase. Las RPC públicas exponen solo resultados, alineaciones, goles y nombres de jugadores; las RPC de administración comprueban el rol `admin` y permiten crear, editar y borrar actas o jugadores. El cliente nunca usa una clave `service_role`.

## Ejecutar el proyecto

### Android

Abre el proyecto con Android Studio, selecciona el módulo `androidApp` y ejecuta la configuración de aplicación. También puedes generar un APK de depuración desde la raíz:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

El APK resultante se genera en `androidApp/build/outputs/apk/debug/`.

### iOS

En macOS, abre `iosApp` con Xcode y ejecuta el esquema `iosApp` en un simulador o dispositivo compatible.

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
