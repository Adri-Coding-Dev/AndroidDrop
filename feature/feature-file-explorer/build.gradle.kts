plugins {
    id("androiddrop.feature")
}

android {
    namespace = "com.androiddrop.feature.fileexplorer"
}

dependencies {
    implementation(project(":domain:domain-usecase"))
    implementation(project(":domain:domain-model"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
