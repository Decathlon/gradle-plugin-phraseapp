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

plugins {
    alias(libs.plugins.sonarqube.gradle)
}

sonar {
    properties {
        property("sonar.projectKey", "Decathlon_gradle-plugin-phraseapp")
        property("sonar.organization", "decathlon")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.pullrequest.provider", "GitHub")
        property("sonar.pullrequest.github.repository", "Decathlon/gradle-plugin-phraseapp")
        property("sonar.coverage.jacoco.xmlReportPaths", "$rootDir/api-gradle/build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml")
        property("sonar.login", System.getenv("SONAR_TOKEN"))
        property("sonar.debug", true)
    }
}
