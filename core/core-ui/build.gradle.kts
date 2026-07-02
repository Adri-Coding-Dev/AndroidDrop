plugins {
    id("androiddrop.core")
    id("androiddrop.compose")
}

android {
    namespace = "com.androiddrop.core.ui"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":domain:domain-model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
