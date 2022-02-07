plugins {
    java
    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "io.github.eroshenkoam"
version = version

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

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            suppressAllPomMetadataWarnings()
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(project.name)
                description.set("Allure test filter for jUnit4.")
                url.set("https://github.com/eroshenkoam/allure-junit4-filter")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("eroshenkoam")
                        name.set("Artem Eroshenko")
                        email.set("eroshenkoam@me.com")
                    }
                }
                scm {
                    developerConnection.set("scm:git:git://github.com/eroshenkoam/allure-junit4-filter")
                    connection.set("scm:git:git://github.com/eroshenkoam/allure-junit4-filter")
                    url.set("https://github.com/eroshenkoam/allure-junit4-filter")
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/eroshenkoam/allure-junit4-filter")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

publishing.publications.named<MavenPublication>("maven") {
    pom {
        from(components["java"])
    }
}

tasks.withType(Javadoc::class) {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

tasks.test {
    useJUnitPlatform()
}
