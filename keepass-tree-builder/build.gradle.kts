import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlinJvm)
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    jacoco
}

val appGroupId = "com.github.aivanovski"
val appArtifactId = "keepass-tree-builder"
val appVersion = libs.versions.appVersion.get()

group = appGroupId
version = appVersion

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jacocoTestReport {
    reports {
        val coverageDir = layout.buildDirectory.dir("reports/coverage").get()
        csv.required.set(true)
        csv.outputLocation.set(coverageDir.file("coverage.csv"))
        html.required.set(true)
        html.outputLocation.set(coverageDir)
    }

    dependsOn(allprojects.map { it.tasks.named<Test>("test") })
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

detekt {
    config.setFrom("../detekt.yml")
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)

    implementation(libs.kotpass)
    implementation(libs.okio)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = appGroupId
            artifactId = appArtifactId
            version = appVersion

            from(components["java"])
        }
    }
}