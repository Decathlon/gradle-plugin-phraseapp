import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
    id("jacoco")
    id("com.vanniktech.maven.publish")
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

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}
