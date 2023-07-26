import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
    id("jacoco")
    id("com.vanniktech.maven.publish")
    alias(libs.plugins.sonarqube.gradle)
}

dependencies {
    api(libs.kotlin.stdlib)
    api(libs.kotlinx.coroutines)

    implementation(platform(libs.square.okhttp.bom))
    implementation(libs.bundles.square.okhttp3)
    implementation(libs.bundles.square.retrofit)
    implementation(libs.google.gson)

    testImplementation(libs.junit)
    testImplementation(libs.assertk)
    testImplementation(libs.mockito.kotlin)
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sonar {
    properties {
        property("sonar.sources", "src/main/kotlin")
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}
