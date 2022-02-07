plugins {
    java
}

group = "io.github.eroshenkoam"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.qameta.allure:allure-java-commons:2.17.2")
    implementation("io.qameta.allure:allure-test-filter:2.17.2")
    implementation("junit:junit:4.13.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
