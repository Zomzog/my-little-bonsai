import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "fr.zomzog.mylittlebonsai"
        compileSdk = 36
        minSdk = 29

        compilations.all {
            compileKotlinTask.compilerOptions {
                jvmTarget.set(JvmTarget.JVM_25)
            }
        }
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
        }

        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.ui.tooling.preview)
        }
    }
}
