// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed


plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.publishPlugin) apply true
    alias(libs.plugins.dokkaGradleplugin) apply true
    alias(libs.plugins.compose.compiler) apply false
}
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
