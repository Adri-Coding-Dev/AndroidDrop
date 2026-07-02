plugins {
    id("androiddrop.domain")
}

android {
    namespace = "com.androiddrop.domain.usecase"
}

dependencies {
    implementation(project(":domain:domain-model"))
    implementation(project(":domain:domain-repository"))
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.coroutines.core)
}
