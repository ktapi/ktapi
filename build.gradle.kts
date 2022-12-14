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
    val kotlinVersion: String by project
    val jacksonVersion: String by project
    val ktormVersion: String by project
    val javalinVersion: String by project

    compileOnly(gradleApi())

    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")
    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("org.yaml:snakeyaml:1.30")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-jackson:$ktormVersion")
    implementation("org.ktorm:ktorm-support-postgresql:$ktormVersion")
    implementation("org.ktorm:ktorm-support-mysql:$ktormVersion")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:9.2.0")
    implementation("io.javalin:javalin:$javalinVersion")
    implementation("io.javalin:javalin-openapi:$javalinVersion")
    implementation("io.javalin:javalin-testtools:$javalinVersion")
    implementation("org.springframework.security:spring-security-crypto:5.7.3")

    compileOnly("io.sentry:sentry-servlet:6.4.1")
    compileOnly("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    compileOnly("io.mockk:mockk:1.12.7")
    compileOnly("redis.clients:jedis:4.2.3")
    compileOnly("com.rabbitmq:amqp-client:5.14.2")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.12.7")
    testImplementation("redis.clients:jedis:4.2.3")
    testImplementation("com.rabbitmq:amqp-client:5.14.2")
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
            version = "0.1.0"

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
