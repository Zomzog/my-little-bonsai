import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(25)

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    jvm()

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(compose.preview)
        }

        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

android {
    namespace = "fr.zomzog.mylittlebonsai"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "fr.zomzog.mylittlebonsai.MainActivity",
                    "fr.zomzog.mylittlebonsai.MainActivity*",
                    "fr.zomzog.mylittlebonsai.MainKt",
                    "fr.zomzog.mylittlebonsai.Main*Kt",
                    "mylittlebonsai.composeapp.generated.resources.*",
                    "*ComposableSingletons*",
                    "*\$WhenMappings",
                    "*\$\$serializer",
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }

        verify {
            rule {
                bound {
                    minValue = 100
                    coverageUnits = CoverageUnit.INSTRUCTION
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}
