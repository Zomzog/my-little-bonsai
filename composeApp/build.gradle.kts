import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

// Skip Android targets when no Android SDK is configured.
// The Android Gradle Plugin only resolves from Google Maven (dl.google.com), which is
// unavailable in some environments (e.g. Claude Code remote sessions).
// Auto-detecting via ANDROID_HOME / ANDROID_SDK_ROOT avoids the need for any
// manual flag — CI with the SDK set up gets Android; environments without it get
// JVM + Wasm only, which is enough to run all commonTest tests.
val hasAndroidSdk = System.getenv("ANDROID_HOME") != null
    || System.getenv("ANDROID_SDK_ROOT") != null

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    if (System.getenv("ANDROID_HOME") != null || System.getenv("ANDROID_SDK_ROOT") != null) {
        alias(libs.plugins.androidMultiplatformLibrary)
        // Compose plugins require the Compose Runtime on the classpath. Only apply them when
        // the full dependency graph (including Google Maven) is available.
        alias(libs.plugins.composeMultiplatform)
        alias(libs.plugins.composeCompiler)
    }
    alias(libs.plugins.kover)
}

if (!hasAndroidSdk) {
    // Compose Multiplatform Desktop (JVM) artifacts declare transitive POM/module
    // dependencies on Android-only androidx.* modules hosted exclusively on Google Maven
    // (dl.google.com, unreachable here). These are NOT needed at JVM runtime — JetBrains
    // ships JVM equivalents under org.jetbrains.androidx.* on Maven Central.
    //
    // A ComponentMetadataRule removes every androidx.* dependency from every component's
    // metadata at resolution time, preventing Gradle from ever trying to fetch those POMs
    // from Google Maven. Using a rule (vs. dozens of exclude() calls) handles all present
    // and future androidx.* groups in one pass.
    dependencies {
        components.all<StripAndroidDependencies>()
    }
}

// Android target is configured in android-target.gradle (Groovy) so that Kotlin DSL
// does not need to compile Android plugin types when the plugin is absent.
if (hasAndroidSdk) {
    apply(from = "android-target.gradle")
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

    sourceSets {
        commonMain {
            if (!hasAndroidSdk) {
                // Exclude all Compose-dependent sources; only pure-Kotlin domain code compiles here.
                // This avoids resolving Compose JVM artifacts from Google Maven (blocked in this env).
                kotlin.exclude(
                    "**/App.kt",
                    "**/HomeScreen.kt",
                    "**/ui/addbonsai/AddBonsaiScreen.kt",
                    "**/ui/addbonsai/BonsaiDatePickerDialog.kt",
                    "**/ui/bonsailist/BonsaiListScreen.kt",
                )
            }
            dependencies {
                if (hasAndroidSdk) {
                    implementation(libs.compose.runtime)
                    implementation(libs.compose.foundation)
                    implementation(libs.compose.material3)
                    implementation(libs.compose.ui)
                    implementation(libs.components.resources)
                }
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            if (!hasAndroidSdk) {
                kotlin.exclude(
                    "**/AppNavigationTest.kt",
                    "**/HomeScreenTest.kt",
                    "**/ui/addbonsai/AddBonsaiScreenTest.kt",
                    "**/ui/bonsailist/BonsaiListScreenTest.kt",
                )
            }
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.assertk)
                if (hasAndroidSdk) {
                    implementation(libs.compose.ui.test)
                }
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmMain by getting {
            if (!hasAndroidSdk) {
                // Exclude the actual for BonsaiDatePickerDialog — its expect is also excluded above.
                kotlin.exclude("**/BonsaiDatePickerDialog.kt")
            }
        }
    }
}

// JVM Compose desktop dependency lives in a Groovy script so that Kotlin DSL does not
// need to reference compose.desktop.currentOs (an extension provided by composeMultiplatform
// plugin, which is only applied when hasAndroidSdk is true).
if (hasAndroidSdk) {
    apply(from = "jvm-compose-dependencies.gradle")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "mylittlebonsai.composeapp.generated.resources.*",
                    "*ComposableSingletons*",
                    "*\$WhenMappings",
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

/**
 * Removes all `androidx.*` transitive dependencies from every resolved component.
 *
 * Compose Multiplatform Desktop artifacts (e.g. material3-desktop) list Android-only
 * androidx.* modules in their POM files. On JVM those modules are never needed —
 * JetBrains ships their JVM re-implementations under org.jetbrains.androidx.*.
 * Without this rule, Gradle tries to fetch the androidx.* POMs from Google Maven
 * (dl.google.com), which is blocked in environments that have no Android SDK.
 */
abstract class StripAndroidDependencies : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.allVariants {
            withDependencies {
                removeAll { it.group.startsWith("androidx.") }
            }
        }
    }
}
