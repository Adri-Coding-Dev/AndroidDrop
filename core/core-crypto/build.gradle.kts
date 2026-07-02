plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.core.crypto"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(libs.conscrypt.android)
    implementation(libs.bouncycastle.bcprov)
    implementation(libs.bouncycastle.bcpkix)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
