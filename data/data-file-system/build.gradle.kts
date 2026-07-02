plugins {
    id("androiddrop.library")
}

android {
    namespace = "com.androiddrop.data.filesystem"
}

dependencies {
    implementation(project(":domain:domain-repository"))
    implementation(project(":domain:domain-model"))
    implementation(project(":core:core-common"))
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
