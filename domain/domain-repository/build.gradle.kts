plugins {
    id("androiddrop.domain")
}

android {
    namespace = "com.androiddrop.domain.repository"
}

dependencies {
    implementation(project(":domain:domain-model"))
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.coroutines.core)
}
