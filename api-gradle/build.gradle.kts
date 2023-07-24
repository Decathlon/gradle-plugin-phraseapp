import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("jacoco")
    id("jacoco-report-aggregation")
    id("com.vanniktech.maven.publish")
    alias(libs.plugins.sonarqube.gradle)
}

sonar {
    properties {
        property("sonar.sources", "src")
    }
}

dependencies {
    implementation(project(":client"))
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

gradlePlugin {
    plugins {
        create("publishing") {
            id = "com.decathlon.phraseapp"
            implementationClass = "phraseapp.PhraseAppPlugin"
        }
    }
}
