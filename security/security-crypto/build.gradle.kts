plugins {
    id("androiddrop.core")
}

android {
    namespace = "com.androiddrop.security.crypto"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-crypto"))
    implementation(libs.bouncycastle.bcprov)
    implementation(libs.bouncycastle.bcpkix)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
