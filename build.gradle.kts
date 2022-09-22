plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.noob"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jsoup:jsoup:1.11.3")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:2.0.0")
}

tasks {

    test {
        useJUnitPlatform()
    }

    jar {
        manifest {
            attributes(Pair("Main-Class", "de.noob.vertretungsplan.MainKt"))
        }
    }

    shadowJar {
        classifier = null
    }

    assemble {
        dependsOn(shadowJar)
    }

}