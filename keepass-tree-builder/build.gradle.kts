import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.9.22")
    id("java-library")
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    jacoco
}

val appGroupId = "com.github.aivanovski"
val appArtifactId = "keepass-tree-builder"
val appVersion = "0.1.0"

group = appGroupId
version = appVersion

tasks.withType<KotlinCompile> {
    kotlinOptions {
        apiVersion = "1.5"
        languageVersion = "1.5"
        jvmTarget = "11"
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
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.2")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.2")
    testImplementation("io.mockk:mockk:1.12.3")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")

    // KeePass
    implementation("com.github.anvell:kotpass:0.7.0")
    implementation("com.squareup.okio:okio:3.2.0")
}
