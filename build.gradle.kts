buildscript {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group.startsWith("org.jetbrains.kotlin")) {
                useVersion("2.3.21")
            }
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
}
