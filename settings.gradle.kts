pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidDrop"

// Build logic - included build for precompiled convention plugins
includeBuild("build-logic")

// Core modules
include(":core:core-common")
include(":core:core-ui")
include(":core:core-network")
include(":core:core-crypto")
include(":core:core-testing")

// Domain modules
include(":domain:domain-model")
include(":domain:domain-repository")
include(":domain:domain-usecase")

// Data modules
include(":data:data-file-system")
include(":data:data-ble")
include(":data:data-wifi-direct")
include(":data:data-nearby")
include(":data:data-transfer")

// Animation modules
include(":animation:animation-engine")
include(":animation:animation-sphere")
include(":animation:animation-portal")
include(":animation:animation-particles")
include(":animation:animation-gesture")

// Security modules
include(":security:security-crypto")
include(":security:security-key-exchange")

// Sync module
include(":sync:sync-protocol")

// Service modules
include(":service:service-discovery")
include(":service:service-transfer")

// Feature modules
include(":feature:feature-file-explorer")
include(":feature:feature-transfer")
include(":feature:feature-discovery")
include(":feature:feature-settings")
include(":feature:feature-diagnostics")

// App module
include(":app")
