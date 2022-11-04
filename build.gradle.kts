buildscript {
    val kotlinVersion: String by project

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
    }
}

group = "org.ktapi"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

plugins {
    `java-library`
    `maven-publish`
}

apply(plugin = "kotlin")

dependencies {
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.ktapi"
            artifactId = "ktapi"
            version = "0.0.1"

            from(components["java"])

            pom {
                name.set("KtAPI")
                description.set("A library making it simple to create APIs in Kotlin")
                url.set("http://ktapi.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("aaronfreeman")
                        name.set("Aaron Freeman")
                        email.set("aaron@freeman.zone")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:ktapi/ktapi.git")
                    url.set("https://github.com/ktapi/ktapi")
                }
            }
        }
    }
}
