plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.animation.sphere"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":animation:animation-engine"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
