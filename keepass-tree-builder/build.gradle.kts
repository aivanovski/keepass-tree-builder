import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlinJvm)
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.detekt)
    jacoco
}

val appGroupId = "com.github.aivanovski"
val appArtifactId = "keepass-tree-builder"
val appVersion = libs.versions.appVersion.get()

group = appGroupId
version = appVersion

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_1_8)
        languageVersion.set(KotlinVersion.KOTLIN_1_8)
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.jacocoTestReport {
    reports {
        val coverageDir = File("$buildDir/reports/coverage")
        csv.required.set(true)
        csv.outputLocation.set(File(coverageDir, "coverage.csv"))
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