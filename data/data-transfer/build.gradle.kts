plugins {
    id("androiddrop.library")
}

android {
    namespace = "com.androiddrop.data.transfer"
}

dependencies {
    implementation(project(":domain:domain-model"))
    implementation(project(":domain:domain-repository"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-crypto"))
    implementation(project(":core:core-network"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
