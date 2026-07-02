import org.gradle.api.artifacts.VersionCatalogsExtension
import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

pluginManager.apply("com.android.library")
pluginManager.apply("org.jetbrains.kotlin.android")
pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

extensions.configure<LibraryExtension> {
    compileSdk = 35
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    (this as ExtensionAware).extensions.configure<KotlinJvmOptions>("kotlinOptions") {
        jvmTarget = "17"
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(libs.findLibrary("javax-inject").get())
    "implementation"(libs.findLibrary("kotlinx-coroutines-core").get())
    "implementation"(libs.findLibrary("kotlinx-serialization-json").get())
    "testImplementation"(libs.findLibrary("junit-jupiter").get())
    "testImplementation"(libs.findLibrary("mockk").get())
    "testImplementation"(libs.findLibrary("turbine").get())
    "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
}
