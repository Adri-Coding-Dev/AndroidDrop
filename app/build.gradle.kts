plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.androiddrop.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.androiddrop.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/AL2.0"
            excludes += "/META-INF/LGPL2.1"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    // Core
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-crypto"))
    implementation(project(":core:core-testing"))

    // Domain
    implementation(project(":domain:domain-model"))
    implementation(project(":domain:domain-repository"))
    implementation(project(":domain:domain-usecase"))

    // Data
    implementation(project(":data:data-file-system"))
    implementation(project(":data:data-ble"))
    implementation(project(":data:data-wifi-direct"))
    implementation(project(":data:data-nearby"))
    implementation(project(":data:data-transfer"))

    // Features
    implementation(project(":feature:feature-file-explorer"))
    implementation(project(":feature:feature-transfer"))
    implementation(project(":feature:feature-discovery"))
    implementation(project(":feature:feature-settings"))
    implementation(project(":feature:feature-diagnostics"))

    // Services
    implementation(project(":service:service-discovery"))
    implementation(project(":service:service-transfer"))

    // Sync & Security
    implementation(project(":sync:sync-protocol"))
    implementation(project(":security:security-crypto"))
    implementation(project(":security:security-key-exchange"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Security
    implementation(libs.conscrypt.android)

    // Kotlin
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Logging
    implementation(libs.timber)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
}
