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

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        val jvmMain by getting {
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
                    // androidMain classes require on-device tests; excluded from JVM coverage
                    "*AndroidFolderStorageManager*",
                    "*.foldersetup.FolderPickerSupportKt\$rememberFolderPickerLauncher\$*",
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

// TEMPORARY diagnostic: prints per-class missed-line counts from the Kover XML report
// to the CI console, since the XML artifact itself isn't reachable from this session.
// Remove once coverage is back above the gate.
tasks.register("koverPrintWorstFiles") {
    dependsOn("koverXmlReport")
    doLast {
        val reportFile = file("build/reports/kover/report.xml")
        if (!reportFile.exists()) {
            println("KOVER_DIAG: report not found at $reportFile")
            return@doLast
        }
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory.setFeature("http://xml.org/sax/features/validation", false)
        val doc = factory.newDocumentBuilder().parse(reportFile)
        val classNodes = doc.getElementsByTagName("class")
        val results = mutableListOf<Triple<String, Int, Int>>()
        for (i in 0 until classNodes.length) {
            val classEl = classNodes.item(i) as org.w3c.dom.Element
            val name = classEl.getAttribute("name")
            val counters = classEl.getElementsByTagName("counter")
            var missed = 0
            var covered = 0
            for (j in 0 until counters.length) {
                val counterEl = counters.item(j) as org.w3c.dom.Element
                if (counterEl.getAttribute("type") == "LINE") {
                    missed = counterEl.getAttribute("missed").toInt()
                    covered = counterEl.getAttribute("covered").toInt()
                }
            }
            if (missed > 0) results += Triple(name, missed, covered)
        }
        println("KOVER_DIAG_START")
        results.sortedByDescending { it.second }.forEach { (name, missed, covered) ->
            println("KOVER_DIAG missed=$missed covered=$covered class=$name")
        }
        println("KOVER_DIAG_END")
    }
}

tasks.named("koverXmlReport") {
    finalizedBy("koverPrintWorstFiles")
}
