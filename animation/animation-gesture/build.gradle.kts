plugins {
    id("androiddrop.core")
    id("androiddrop.compose")
}

android {
    namespace = "com.androiddrop.animation.gesture"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
