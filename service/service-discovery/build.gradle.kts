plugins {
    id("androiddrop.library")
}

android {
    namespace = "com.androiddrop.service.discovery"
}

dependencies {
    implementation(project(":domain:domain-usecase"))
    implementation(project(":domain:domain-repository"))
    implementation(project(":domain:domain-model"))
    implementation(project(":core:core-common"))
    implementation(project(":data:data-ble"))
    implementation(project(":data:data-nearby"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
