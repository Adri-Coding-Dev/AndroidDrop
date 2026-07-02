plugins {
    id("androiddrop.library")
}

android {
    namespace = "com.androiddrop.service.transfer"
}

dependencies {
    implementation(project(":domain:domain-usecase"))
    implementation(project(":domain:domain-repository"))
    implementation(project(":domain:domain-model"))
    implementation(project(":core:core-common"))
    implementation(project(":data:data-transfer"))
    implementation(project(":data:data-wifi-direct"))
    implementation(project(":core:core-crypto"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
