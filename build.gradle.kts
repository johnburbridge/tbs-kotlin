plugins {
    kotlin("jvm") version "1.9.20"
    `java-library`
    `maven-publish`
    application
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

group = "com.github.johnburbridge"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin standard library
    implementation(kotlin("stdlib"))
    
    // SortedMap implementation for Red-Black Tree
    implementation("org.apache.commons:commons-collections4:4.4")
    
    // B-Tree implementation
    implementation("org.apache.commons:commons-collections4:4.4")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/test/jacocoTestReport.xml"))
        csv.required.set(false)
        html.required.set(true)
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
    baseline = file("$projectDir/config/baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "11"
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.github.johnburbridge.tbs.examples.PerformanceBenchmark")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
} 