plugins {
    id("androiddrop.core")
    id("androiddrop.compose")
}

android {
    namespace = "com.androiddrop.animation.particles"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":animation:animation-engine"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
