plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.animation.engine"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
