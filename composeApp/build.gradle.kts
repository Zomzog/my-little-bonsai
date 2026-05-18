import org.jetbrains.compose.ExperimentalComposeLibrary
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "fr.zomzog.mylittlebonsai"
        compileSdk = 36
        minSdk = 29
    }

    jvm()

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        outputModuleName = "composeApp"
        browser()
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
            api(libs.androidx.activity.compose)
        }

        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
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
