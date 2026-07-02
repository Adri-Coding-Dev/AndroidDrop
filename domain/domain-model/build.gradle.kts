plugins {
    id("androiddrop.domain")
}

android {
    namespace = "com.androiddrop.domain.model"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.serialization.json)
}
