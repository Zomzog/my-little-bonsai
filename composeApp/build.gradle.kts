import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    jvm()

    android {
        namespace = "fr.zomzog.mylittlebonsai"
        compileSdk = 37
        minSdk = 29

        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.components.resources)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        // Shared between jvm and android only (not wasmJs), for `java.text.Normalizer`-based
        // code — identical actuals in both platform source sets otherwise.
        val jvmAndAndroidMain by creating {
            dependsOn(commonMain.get())
        }

        val androidMain by getting {
            dependsOn(jvmAndAndroidMain)
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        val jvmMain by getting {
            dependsOn(jvmAndAndroidMain)
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

kover {
    // androidMain uses Android SAF/Compose APIs that require on-device testing and
    // cannot be reached by the JVM Kover task; exclude it so the report is
    // consistent with wasmJsMain (which is also not instrumented by JVM Kover).
    currentProject {
        sources {
            excludedSourceSets.add("androidMain")
        }
    }

    reports {
        filters {
            excludes {
                classes(
                    "mylittlebonsai.composeapp.generated.resources.*",
                    "*ComposableSingletons*",
                    "*\$WhenMappings",
                    // androidMain classes require on-device tests; excluded from JVM coverage.
                    // `excludedSourceSets` above does not stop the AGP KMP library plugin from
                    // still handing these compiled classes to the JVM Kover report, so each one
                    // needs an explicit filter too.
                    "*AndroidFolderStorageManager*",
                    "*.foldersetup.FolderPickerSupportKt\$rememberFolderPickerLauncher\$*",
                    // SafVaultFileSystem.kt itself is now just a thin ContentResolver/
                    // DocumentsContract adapter (AndroidDocumentTree) behind the DocumentTree
                    // interface; the document-id resolution, tmp-then-rename write and
                    // caching logic moved to commonMain's DocumentTreeVaultFileSystem, which
                    // is covered via commonTest against a fake and needs no exclusion.
                    "*SafVaultFileSystem*",
                    "*AndroidDocumentTree*",
                    "*.data.BonsaiRepositoryProvider_androidKt",
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }

        verify {
            rule {
                bound {
                    minValue = 95
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}
