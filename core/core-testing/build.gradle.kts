plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.core.testing"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.junit)
    implementation(libs.junit.jupiter)
    implementation(libs.mockk)
    implementation(libs.turbine)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.coroutines.core)
}
