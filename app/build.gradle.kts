plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.vnchflex.vietkeyboard"
    compileSdk = 35

    val ciRunNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
    val computedVersionCode = if (ciRunNumber != null) 100 + ciRunNumber else 100

    defaultConfig {
        applicationId = "com.vnchflex.vietkeyboard"
        minSdk = 26
        targetSdk = 35
        versionCode = computedVersionCode
        versionName = "1.0.$computedVersionCode"
    }

    signingConfigs {
        create("persistent") {
            storeFile = file("vnchflex-signing.jks")
            storePassword = "vnchflex123"
            keyAlias = "vnchflex"
            keyPassword = "vnchflex123"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("persistent")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("persistent")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    testImplementation("junit:junit:4.13.2")
}
