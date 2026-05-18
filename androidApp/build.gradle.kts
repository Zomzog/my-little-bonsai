plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "fr.zomzog.mylittlebonsai.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.zomzog.mylittlebonsai"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":composeApp"))
}
