import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("jacoco")
    id("jacoco-report-aggregation")
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(project(":client"))
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

gradlePlugin {
    plugins {
        create("phrase") {
            id = "com.decathlon.phrase"
            implementationClass = "phrase.PhrasePlugin"
        }
    }
}
