# Documentación - Aplicación de Autenticación Biométrica Android

## 📋 Índice
1. [Descripción General](#descripción-general)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Sensores Biométricos en Android](#sensores-biométricos-en-android)
4. [Componentes del Proyecto](#componentes-del-proyecto)
5. [Implementación Detallada](#implementación-detallada)
6. [Dependencias y Configuración](#dependencias-y-configuración)
7. [Flujo de Usuario](#flujo-de-usuario)
8. [Gestión de Permisos](#gestión-de-permisos)

---

## 📱 Descripción General

Esta aplicación Android nativa implementa **autenticación biométrica dual** utilizando:
- **Huella dactilar** (sensor biométrico nativo)
- **Reconocimiento facial** (cámara frontal personalizada)

### Tecnologías Utilizadas
- **Kotlin** como lenguaje principal
- **XML** para interfaces de usuario
- **AndroidX Biometric** para huella dactilar
- **CameraX** para reconocimiento facial
- **API mínima**: Android 6.0 (API 23)

---

## 🏗️ Arquitectura del Proyecto

```
src/main/
├── java/com/example/sensorfacial_biometrico/
│   ├── MainActivity.kt          # Pantalla principal con opciones de autenticación
│   ├── FaceAuthActivity.kt      # Reconocimiento facial con cámara
│   └── WelcomeActivity.kt       # Pantalla post-autenticación
├── res/
│   ├── layout/
│   │   ├── activity_main.xml         # Layout pantalla principal
│   │   ├── activity_face_auth.xml    # Layout cámara facial
│   │   └── activity_welcome.xml      # Layout pantalla bienvenida
│   └── drawable/
│       └── face_frame.xml            # Marco oval para rostro
└── AndroidManifest.xml          # Configuración y permisos
```

---

## 🔒 Sensores Biométricos en Android

### 1. **BiometricPrompt** - Huella Dactilar
Android proporciona la clase `BiometricPrompt` que interactúa directamente con los **sensores biométricos del hardware**:

#### ¿Qué es?
- **API unificada** para acceder a sensores biométricos
- **Integración nativa** con el sistema operativo
- **Seguridad a nivel de hardware** (Trusted Execution Environment)

#### ¿Cómo funciona?
1. **Detección automática** del tipo de sensor disponible
2. **Validación** contra datos biométricos almacenados en el TEE
3. **Respuesta criptográfica** segura del sistema

#### Tipos de Autenticadores
```kotlin
// Autenticación débil (incluye patrones, PIN)
BiometricManager.Authenticators.BIOMETRIC_WEAK

// Autenticación fuerte (solo biométricos seguros)
BiometricManager.Authenticators.BIOMETRIC_STRONG
```

### 2. **Reconocimiento Facial Personalizado**
Como muchos dispositivos no tienen **Face ID nativo**, implementamos una solución personalizada:

#### ¿Por qué personalizada?
- **BiometricPrompt** solo funciona con sensores biométricos certificados
- **Muchos dispositivos** carecen de hardware de reconocimiento facial
- **Flexibilidad** para implementar algoritmos propios

#### Componentes utilizados
- **CameraX**: API moderna de cámara
- **Cámara frontal**: Para captura de selfies
- **Simulación**: Procesamiento básico (demo)

---

## 🧩 Componentes del Proyecto

### 1. **MainActivity.kt** - Pantalla Principal

#### Responsabilidades
- **Punto de entrada** de la aplicación
- **Verificación** de disponibilidad de sensores
- **Gestión** de permisos de cámara
- **Navegación** entre actividades

#### Componentes Clave

##### BiometricManager
```kotlin
val biometricManager = BiometricManager.from(this)
```
- **Función**: Verificar disponibilidad de sensores biométricos
- **Uso**: Habilitar/deshabilitar botones según capacidades del dispositivo
- **Ubicación**: Método `checkAvailability()`

##### BiometricPrompt
```kotlin
biometricPrompt = BiometricPrompt(this, executor, authenticationCallback)
```
- **Función**: Interfaz de autenticación biométrica
- **Uso**: Mostrar diálogo nativo de huella dactilar
- **Ubicación**: Método `setupBiometricPrompt()`

##### Executor
```kotlin
executor = ContextCompat.getMainExecutor(this)
```
- **Función**: Hilo principal para callbacks de UI
- **Uso**: Ejecutar respuestas de autenticación en UI thread
- **Ubicación**: Inicialización en `onCreate()`

### 2. **FaceAuthActivity.kt** - Reconocimiento Facial

#### Responsabilidades
- **Activación** de cámara frontal
- **Captura** de imagen facial
- **Simulación** de reconocimiento
- **Feedback** visual al usuario

#### Componentes Clave

##### ProcessCameraProvider (CameraX)
```kotlin
val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
```
- **Función**: Proveedor de servicios de cámara
- **Uso**: Inicializar y configurar cámara
- **Ubicación**: Método `startCamera()`

##### Preview
```kotlin
val preview = Preview.Builder().build()
```
- **Función**: Vista previa en tiempo real de la cámara
- **Uso**: Mostrar imagen en vivo en PreviewView
- **Ubicación**: Configuración de casos de uso

##### ImageCapture
```kotlin
imageCapture = ImageCapture.Builder().build()
```
- **Función**: Capturar fotografías
- **Uso**: Tomar foto para procesamiento (futuro)
- **Ubicación**: Preparación para análisis facial

##### CameraSelector
```kotlin
val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
```
- **Función**: Seleccionar cámara frontal
- **Uso**: Forzar uso de cámara delantera para selfies
- **Ubicación**: Configuración de cámara

### 3. **WelcomeActivity.kt** - Pantalla de Éxito

#### Responsabilidades
- **Confirmación** de autenticación exitosa
- **Mostrar** método utilizado
- **Navegación** de regreso al login

#### Componentes Clave
- **Intent extras**: Recibir método de autenticación usado
- **Navigation**: Limpieza de stack de actividades

---

## 🔧 Implementación Detallada

### 1. **Verificación de Disponibilidad Biométrica**

```kotlin
// Ubicación: MainActivity.kt - checkAvailability()
when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
    BiometricManager.BIOMETRIC_SUCCESS -> {
        // Sensor disponible y configurado
        btnFingerprint.isEnabled = true
    }
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
        // Sin hardware biométrico
        btnFingerprint.isEnabled = false
    }
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
        // Sin datos biométricos registrados
        btnFingerprint.isEnabled = false
    }
}
```

**¿Qué hace?**
- **Detecta** si el dispositivo tiene sensor de huellas
- **Verifica** si hay huellas registradas
- **Adapta** la interfaz según capacidades

### 2. **Configuración de BiometricPrompt**

```kotlin
// Ubicación: MainActivity.kt - setupBiometricPrompt()
fingerprintPromptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Autenticación con huella")
    .setSubtitle("Usa tu huella digital para autenticarte")
    .setDescription("Coloca tu dedo en el sensor de huellas dactilares")
    .setNegativeButtonText("Cancelar")
    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
    .build()
```

**Configuración detallada:**
- **setTitle()**: Título del diálogo
- **setSubtitle()**: Texto secundario
- **setDescription()**: Instrucciones para el usuario
- **setNegativeButtonText()**: Botón de cancelación
- **setAllowedAuthenticators()**: Tipos de autenticación permitidos

### 3. **Callbacks de Autenticación**

```kotlin
// Ubicación: MainActivity.kt - BiometricPrompt.AuthenticationCallback
override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    // Autenticación exitosa - navegar a bienvenida
    navigateToWelcomeScreen()
}

override fun onAuthenticationFailed() {
    // Huella no reconocida - mostrar mensaje
    showToast("Huella no reconocida")
}

override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    // Error del sistema - mostrar detalles
    showToast("Error de autenticación: $errString")
}
```

### 4. **Configuración de Cámara**

```kotlin
// Ubicación: FaceAuthActivity.kt - startCamera()
try {
    cameraProvider.unbindAll()  // Limpiar usos anteriores
    cameraProvider.bindToLifecycle(
        this,                   // LifecycleOwner
        cameraSelector,         // Cámara frontal
        preview,               // Vista previa
        imageCapture           // Captura de imágenes
    )
} catch (exc: Exception) {
    // Manejo de errores
}
```

**Proceso:**
1. **Unbind** casos de uso anteriores
2. **Bind** nuevos casos de uso al lifecycle
3. **Configurar** preview y captura
4. **Manejar** errores de configuración

---

## 📦 Dependencias y Configuración

### 1. **build.gradle (Module: app)**

```kotlin
dependencies {
    // Biometría
    implementation 'androidx.biometric:biometric:1.1.0'
    
    // Interfaz de usuario
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    // CameraX para reconocimiento facial
    implementation "androidx.camera:camera-core:1.3.0"
    implementation "androidx.camera:camera-camera2:1.3.0"
    implementation "androidx.camera:camera-lifecycle:1.3.0"
    implementation "androidx.camera:camera-view:1.3.0"
}
```

### 2. **AndroidManifest.xml - Permisos**

```xml
<!-- Biometría -->
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.USE_FINGERPRINT" />

<!-- Cámara -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Características de hardware -->
<uses-feature android:name="android.hardware.camera" android:required="true" />
<uses-feature android:name="android.hardware.camera.front" android:required="false" />
```

**Explicación de permisos:**
- **USE_BIOMETRIC**: Acceso a sensores biométricos (API 28+)
- **USE_FINGERPRINT**: Compatibilidad con versiones anteriores
- **CAMERA**: Acceso a cámara del dispositivo
- **uses-feature**: Declarar dependencias de hardware

---

## 🎯 Flujo de Usuario

### 1. **Pantalla Principal (MainActivity)**
```
Usuario abre app
    ↓
Verificar sensores disponibles
    ↓
Mostrar botones habilitados/deshabilitados
    ↓
Usuario selecciona método de autenticación
```

### 2. **Flujo de Huella Dactilar**
```
Presionar "Autenticar con Huella"
    ↓
Mostrar BiometricPrompt nativo
    ↓
Usuario coloca dedo en sensor
    ↓
Sistema valida huella
    ↓
Éxito: Navegar a bienvenida
Fallo: Mostrar mensaje de error
```

### 3. **Flujo de Reconocimiento Facial**
```
Presionar "Autenticar con Rostro"
    ↓
Verificar permiso de cámara
    ↓
Abrir FaceAuthActivity
    ↓
Inicializar cámara frontal
    ↓
Usuario posiciona rostro y presiona "Capturar"
    ↓
Simular procesamiento (2 segundos)
    ↓
Éxito: Regresar a MainActivity → Navegar a bienvenida
Fallo: Permitir reintento
```

### 4. **Pantalla de Bienvenida**
```
Mostrar mensaje de éxito
    ↓
Indicar método usado
    ↓
Botón "Cerrar Sesión" → Regresar al inicio
```

---

## 🔐 Gestión de Permisos

### 1. **Permisos de Biometría**
```kotlin
// Automáticos - No requieren solicitud runtime
android.permission.USE_BIOMETRIC
android.permission.USE_FINGERPRINT
```

### 2. **Permisos de Cámara**
```kotlin
// Ubicación: MainActivity.kt
private fun checkCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun requestCameraPermission() {
    ActivityCompat.requestPermissions(
        this, arrayOf(Manifest.permission.CAMERA),
        CAMERA_PERMISSION_REQUEST_CODE
    )
}
```

### 3. **Manejo de Respuestas**
```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<out String>, grantResults: IntArray
) {
    when (requestCode) {
        CAMERA_PERMISSION_REQUEST_CODE -> {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
            }
        }
    }
}
```

---

## 🎨 Componentes de Interfaz

### 1. **Layouts XML**

#### activity_main.xml
- **LinearLayout**: Contenedor principal vertical centrado
- **TextView**: Título de la aplicación
- **Button (2)**: Opciones de autenticación

#### activity_face_auth.xml
- **RelativeLayout**: Contenedor superpuesto
- **PreviewView**: Vista de cámara en tiempo real
- **LinearLayouts**: Paneles superior e inferior con controles
- **View**: Marco oval para posicionar rostro

#### activity_welcome.xml
- **LinearLayout**: Contenedor vertical centrado
- **ImageView**: Ícono de éxito
- **TextViews**: Mensajes de bienvenida y método usado
- **Button**: Cerrar sesión

### 2. **Recursos Drawable**

#### face_frame.xml
```xml
<shape android:shape="oval">
    <stroke android:width="4dp" android:color="#4CAF50" 
            android:dashWidth="10dp" android:dashGap="5dp" />
    <solid android:color="#20FFFFFF" />
</shape>
```
- **Marco oval** con borde verde discontinuo
- **Fondo semitransparente** para guiar posicionamiento

---

## 📱 Consideraciones de Dispositivos

### 1. **Compatibilidad de Sensores**
- **Huella dactilar**: Dispositivos con sensor físico o en pantalla
- **Reconocimiento facial**: Cualquier dispositivo con cámara frontal

### 2. **Versiones de Android**
- **API mínima**: 23 (Android 6.0)
- **BiometricPrompt**: Disponible desde API 28, con compatibilidad hacia atrás

### 3. **Limitaciones Conocidas**
- **Face ID nativo**: Solo en dispositivos premium con hardware específico
- **Seguridad facial**: La implementación actual es demo, no producción
- **Rendimiento**: Dependiente de capacidades del dispositivo

---

## 🚀 Posibles Mejoras

### 1. **Reconocimiento Facial Real**
- **ML Kit Face Detection**: Google ML Kit para detección facial
- **Firebase ML**: Reconocimiento facial en la nube
- **TensorFlow Lite**: Modelo local de reconocimiento

### 2. **Seguridad Adicional**
- **Liveness detection**: Detectar si es una persona real
- **Anti-spoofing**: Prevenir fotos o videos falsos
- **Cifrado**: Almacenamiento seguro de templates faciales

### 3. **UX/UI Mejoras**
- **Animaciones**: Transiciones fluidas entre pantallas
- **Temas**: Modo oscuro/claro
- **Localización**: Soporte multiidioma

---

## 📚 Referencias y Documentación

### APIs Utilizadas
- [AndroidX Biometric](https://developer.android.com/jetpack/androidx/releases/biometric)
- [CameraX](https://developer.android.com/training/camerax)
- [BiometricManager](https://developer.android.com/reference/androidx/biometric/BiometricManager)
- [BiometricPrompt](https://developer.android.com/reference/androidx/biometric/BiometricPrompt)

### Guías Oficiales
- [Android Biometric Authentication](https://developer.android.com/training/sign-in/biometric-auth)
- [Camera and Camera2 APIs](https://developer.android.com/training/camera)
- [Requesting Permissions](https://developer.android.com/training/permissions/requesting)

---

Esta documentación proporciona una comprensión completa del proyecto, desde los conceptos básicos de sensores biométricos hasta la implementación específica de cada componente.
