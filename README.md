# Hattitriki

Aplicación multiplataforma para llevar los resultados, estadísticas y clasificaciones de una liga de fútbol amistosa.

Construida con Kotlin Multiplatform y Compose Multiplatform, comparte la interfaz y la lógica entre Android e iOS. Actualmente incluye datos de ejemplo para poder explorar la experiencia sin depender de un servicio externo.

> Estado: en desarrollo. La consulta de partidos y estadísticas está disponible; la edición de datos y el acceso de administrador están preparados visualmente, pero aún no se conectan a Firebase.

## Funcionalidades

- Panel de inicio con el último resultado y métricas de la temporada.
- Histórico de partidos y acceso al acta de cada encuentro.
- Clasificaciones de jugadores por estadísticas.
- Detalle de partidos, goles y participación de porteros.
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

- Conectar la autenticación de administrador con Firebase Auth.
- Persistir jugadores y partidos en Firestore o Supabase.
- Permitir crear y editar partidos desde la zona de administración.
- Sustituir los datos de muestra por datos sincronizados de la liga.

## Licencia

Este repositorio no incluye una licencia explícita. Todos los derechos quedan reservados hasta que se añada una licencia.
