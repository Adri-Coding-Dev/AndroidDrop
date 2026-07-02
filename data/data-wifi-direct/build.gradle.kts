plugins {
    id("androiddrop.library")
}

android {
    namespace = "com.androiddrop.data.wifidirect"
}

dependencies {
    implementation(project(":domain:domain-model"))
    implementation(project(":domain:domain-repository"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-network"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
