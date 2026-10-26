import java.io.File

plugins {
    id("com.android.application")
}

// Keep build output out of OneDrive to avoid AccessDenied during Gradle tasks.
layout.buildDirectory.set(
    File(System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home"), "QuantumRealm-build")
)

val apiBaseUrl: String =
    (project.findProperty("API_BASE_URL") as String?) ?: "http://10.0.2.2:8000"

android {
    namespace = "com.example.quantumrealm"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.quantumrealm"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.android.volley:volley:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    implementation ("com.airbnb.android:lottie:6.4.0")
    implementation ("com.google.android.gms:play-services-base:18.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}