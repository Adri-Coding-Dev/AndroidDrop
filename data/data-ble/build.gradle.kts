plugins {
    id("androiddrop.library")
}

android {
    namespace = "com.androiddrop.data.ble"
}

dependencies {
    implementation(project(":domain:domain-repository"))
    implementation(project(":domain:domain-model"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-crypto"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
