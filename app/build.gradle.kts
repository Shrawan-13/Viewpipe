import java.io.File
import java.io.StringWriter
import java.io.PrintWriter
import java.net.URL
import java.net.HttpURLConnection
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.HttpsURLConnection

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.newpipe.player.kdjfe"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("debugConfig")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.coil.compose)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui)
  implementation(libs.newpipe.extractor)
  
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

val rootDirFile = rootDir

tasks.register("copyApkToRoot") {
    val sourceFile = File(rootDirFile, "app/build/outputs/apk/debug/app-debug.apk")
    val destFile = File(rootDirFile, "app-debug.apk")

    doLast {
        if (sourceFile.exists()) {
            sourceFile.copyTo(destFile, overwrite = true)
            val sizeInBytes = destFile.length()
            val sizeInMb = sizeInBytes.toDouble() / (1024 * 1024)
            println("Successfully copied APK to root: ${destFile.absolutePath}")
            println("APK File Size: $sizeInBytes bytes (approx. ${String.format("%.2f", sizeInMb)} MB)")
        } else {
            println("Warning: APK file not found at ${sourceFile.absolutePath}")
        }
    }
}

tasks.register("uploadApk") {
    dependsOn("copyApkToRoot")

    doLast {
        val destFile = File(project.rootDir, "app-debug.apk")
        val linkFile = File(project.rootDir, "upload_link.txt")
        val logFile = File(project.rootDir, "upload_log.txt")
        val logBuilder = StringBuilder()

        fun log(msg: String) {
            println(msg)
            logBuilder.append(msg).append("\n")
        }

        if (!destFile.exists()) {
            log("Error: APK file does not exist at ${destFile.absolutePath}")
            logFile.writeText(logBuilder.toString())
            return@doLast
        }
        
        log("Uploading ${destFile.length()} bytes to file.io using native HttpURLConnection...")
        try {
            // Setup trust-all certificate logic just in case system clock/certs are out of sync
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                override fun checkClientTrusted(certs: Array<X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>?, authType: String?) {}
            })
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }

            val url = URI.create("https://file.io").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 90000
            
            val boundary = "Boundary-" + System.currentTimeMillis()
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            
            connection.outputStream.use { output ->
                output.write(("--$boundary\r\n").toByteArray())
                output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"app-debug.apk\"\r\n").toByteArray())
                output.write(("Content-Type: application/vnd.android.package-archive\r\n\r\n").toByteArray())
                
                val totalBytes = destFile.length()
                var uploadedBytes = 0L
                val buffer = ByteArray(65536) // 64KB chunks
                var lastProgressUpdate = 0L
                
                destFile.inputStream().use { input ->
                    var bytesRead = input.read(buffer)
                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead)
                        uploadedBytes += bytesRead
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastProgressUpdate > 3000) {
                            val percentage = (uploadedBytes * 100) / totalBytes
                            log("Progress: $percentage% ($uploadedBytes / $totalBytes bytes uploaded)")
                            lastProgressUpdate = currentTime
                        }
                        bytesRead = input.read(buffer)
                    }
                }
                
                output.write(("\r\n--$boundary--\r\n").toByteArray())
                output.flush()
            }
            
            val responseCode = connection.responseCode
            log("HTTP Response Code: $responseCode")
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: "No response body"
            log("Server Response:\n$responseText")
            
            val jsonUrlRegex = Regex("\"link\"\\s*:\\s*\"([^\"]+)\"")
            val match = jsonUrlRegex.find(responseText)
            val extractedLink = match?.groupValues?.get(1)?.replace("\\/", "/")
            
            if (responseCode in 200..299 && extractedLink != null && extractedLink.startsWith("http")) {
                linkFile.writeText(extractedLink)
                log("--- UPLOAD SUCCESSFUL ---")
                log("Saved Transfer Link to ${linkFile.absolutePath}")
                log("Download Link: $extractedLink")
                log("-------------------------")
            } else {
                log("Upload failed: Response status $responseCode. Body does not contain valid link.")
            }
        } catch (e: Exception) {
            log("Upload failed with exception: ${e.message}")
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            log(sw.toString())
        } finally {
            logFile.writeText(logBuilder.toString())
        }
    }
}

tasks.whenTaskAdded {
    if (name == "assembleDebug") {
        finalizedBy("copyApkToRoot")
    }
}
