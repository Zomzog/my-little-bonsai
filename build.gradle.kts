val hasAndroidSdk = System.getenv("ANDROID_HOME") != null
    || System.getenv("ANDROID_SDK_ROOT") != null

plugins {
    if (System.getenv("ANDROID_HOME") != null || System.getenv("ANDROID_SDK_ROOT") != null) {
        alias(libs.plugins.androidApplication) apply false
        alias(libs.plugins.androidMultiplatformLibrary) apply false
    }
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kover) apply false
}
