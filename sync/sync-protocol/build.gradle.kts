plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.sync.protocol"
}

dependencies {
    implementation(project(":domain:domain-model"))
    implementation(project(":domain:domain-repository"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-network"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
