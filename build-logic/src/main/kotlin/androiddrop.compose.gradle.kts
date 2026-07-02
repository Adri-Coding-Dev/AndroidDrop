import org.gradle.api.artifacts.VersionCatalogsExtension
import com.android.build.gradle.LibraryExtension

pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

extensions.configure<LibraryExtension> {
    buildFeatures {
        this.compose = true
    }
}

dependencies {
    "implementation"(platform(libs.findLibrary("compose-bom").get()))
    "implementation"(libs.findLibrary("compose-ui").get())
    "implementation"(libs.findLibrary("compose-ui-graphics").get())
    "implementation"(libs.findLibrary("compose-ui-tooling-preview").get())
    "implementation"(libs.findLibrary("compose-material3").get())
    "implementation"(libs.findLibrary("compose-material-icons-extended").get())
}
