plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.security.keyexchange"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-crypto"))
    implementation(project(":core:core-network"))
    implementation(project(":security:security-crypto"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
