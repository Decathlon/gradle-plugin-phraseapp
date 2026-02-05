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
        property("sonar.sources", "src/main/kotlin")
    }
}

dependencies {
    implementation(project(":client"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("phrase") {
            id = "com.decathlon.phrase"
            implementationClass = "phrase.PhrasePlugin"
        }
    }
}
