import java.io.File
import java.net.URI
import java.security.KeyStore
import java.security.cert.X509Certificate

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Genera el código de Room a partir de las anotaciones.
    alias(libs.plugins.ksp)
}

val releaseStore = providers.environmentVariable("NOVA_KEYSTORE_PATH").orNull
val releaseStorePassword = providers.environmentVariable("NOVA_KEYSTORE_PASSWORD").orNull
val releaseAlias = providers.environmentVariable("NOVA_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("NOVA_KEY_PASSWORD").orNull
val releaseSigningReady = listOf(releaseStore, releaseStorePassword, releaseAlias, releaseKeyPassword).all { !it.isNullOrBlank() }
val allowUnsignedRelease = providers.gradleProperty("NOVA_ALLOW_UNSIGNED_RELEASE").orNull == "true"
val releaseUrl = providers.environmentVariable("NOVA_API_BASE_URL").orNull?.takeIf { it.isNotBlank() } ?: "https://mindflow-production-e4b0.up.railway.app/"

android {
    namespace = "com.mindflow.nova"
    compileSdk {
        version = release(37) {
        }
    }

    defaultConfig {
        applicationId = "com.mindflow.nova"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SENTRY_DSN", "\"${providers.environmentVariable("NOVA_SENTRY_DSN").orElse("").get()}\"")

        // Client ID de tipo "Aplicacion web" en Google Cloud (Google Auth Platform > Clientes).
        // No es secreto: es el mismo que el backend usa como GOOGLE_CLIENT_ID para
        // verificar el idToken. Hay ademas un cliente tipo "Android" (paquete +
        // huella SHA-1 del certificado de firma) que no se referencia en código:
        // solo tiene que existir para que Google confie en el APK que pide el token.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"205907758061-vb4ehb8gu7fcps0mc85l4tsiq2qm4c5j.apps.googleusercontent.com\""
        )
    }

    signingConfigs {
        if (releaseSigningReady) {
            create("production") {
                storeFile = file(releaseStore!!)
                storePassword = releaseStorePassword
                keyAlias = releaseAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // Alias especial que, dentro del emulador, apunta al localhost de la compu.
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$releaseUrl\"")
            if (releaseSigningReady) signingConfig = signingConfigs.getByName("production")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

abstract class VerifyReleaseConfiguration : DefaultTask() {
    @get:Input abstract val backendUrl: Property<String>
    @get:Input abstract val allowUnsigned: Property<Boolean>
    @get:Optional @get:Input abstract val keystorePath: Property<String>

    @TaskAction fun verify() {
        val uri = URI(backendUrl.get())
        check(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null && backendUrl.get().endsWith("/")) {
            "NOVA_API_BASE_URL debe ser HTTPS, sin credenciales y terminar en /."
        }
        val storePassword = System.getenv("NOVA_KEYSTORE_PASSWORD")
        val alias = System.getenv("NOVA_KEY_ALIAS")
        val keyPassword = System.getenv("NOVA_KEY_PASSWORD")
        val ready = keystorePath.isPresent && listOf(storePassword, alias, keyPassword).all { !it.isNullOrBlank() }
        check(ready || allowUnsigned.get()) {
            "Configura las cuatro variables NOVA_KEYSTORE_*/NOVA_KEY_*. Solo CI permite -PNOVA_ALLOW_UNSIGNED_RELEASE=true."
        }
        if (ready) {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            File(keystorePath.get()).inputStream().use { keyStore.load(it, storePassword!!.toCharArray()) }
            val certificate = keyStore.getCertificate(alias) as? X509Certificate
            check(certificate != null && !certificate.subjectX500Principal.name.contains("Android Debug", ignoreCase = true)) {
                "Release necesita un certificado de producción, no Android Debug."
            }
            certificate.checkValidity()
        }
    }
}
val verifyReleaseConfiguration = tasks.register<VerifyReleaseConfiguration>("verifyReleaseConfiguration") {
    backendUrl.set(releaseUrl)
    allowUnsigned.set(allowUnsignedRelease)
    if (!releaseStore.isNullOrBlank()) keystorePath.set(file(releaseStore).absolutePath)
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(verifyReleaseConfiguration) }

dependencies {
    implementation("io.sentry:sentry-android:8.24.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    // Guarda el token de sesión cifrado en el dispositivo.
    implementation("androidx.security:security-crypto:1.1.0")
    // Login con Google: Credential Manager es la API recomendada actual
    // (reemplaza al viejo GoogleSignInClient), y necesita el puente a
    // Play Services más la librería que arma el GoogleIdTokenCredential.
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    // Splash del sistema con el ícono en todas las versiones desde minSdk 24.
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // ViewModel de las pantallas: guarda el estado y coordina las llamadas al backend.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:${libs.versions.lifecycleRuntimeKtx.get()}")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.compose.material:material-icons-extended")
    // Base local: guarda la ruta, las misiones descargadas y los resultados que
    // todavía no se subieron, para poder jugar sin conexión.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Sube los resultados pendientes en segundo plano cuando vuelve la conexión.
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    // Para probar los ViewModel: reemplaza el hilo principal de Android en los tests de JVM.
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.room:room-testing:${libs.versions.room.get()}")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
