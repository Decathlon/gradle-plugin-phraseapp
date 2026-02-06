buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlin.gradle)
        classpath(libs.jacoco.gradle)
        classpath(libs.vanniktech.maven.publish.gradle)
    }
}
