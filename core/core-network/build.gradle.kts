plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.core.network"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.timber)
}
