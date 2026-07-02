plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
