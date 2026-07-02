import org.gradle.api.artifacts.VersionCatalogsExtension

pluginManager.apply("androiddrop.library")
pluginManager.apply("androiddrop.compose")

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(libs.findLibrary("hilt-navigation-compose").get())
    "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
    "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    "implementation"(libs.findLibrary("androidx-navigation-compose").get())
}
