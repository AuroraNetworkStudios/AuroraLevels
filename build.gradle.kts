import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI
import java.util.*

fun loadProperties(filename: String): Properties {
    val properties = Properties()
    if (!file(filename).exists()) {
        return properties
    }
    file(filename).inputStream().use { properties.load(it) }
    return properties
}

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("maven-publish")
}

group = "gg.auroramc"
version = "1.1.0-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21


repositories {
    mavenCentral()
    mavenLocal()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.auroramc.gg/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("gg.auroramc:Aurora:1.2.0")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("dev.aurelium:auraskills-api-bukkit:2.0.7")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("AuroraLevels-${project.version}.jar")

    relocate("co.aikar.commands", "gg.auroramc.levels.libs.acf")
    relocate("co.aikar.locales", "gg.auroramc.levels.libs.locales")

    exclude("acf-*.properties")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

val publishing = loadProperties("publish.properties")

publishing {
    repositories {
        maven {
            name = "AuroraMC"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                URI.create("https://repo.auroramc.gg/repository/maven-snapshots/")
            } else {
                URI.create("https://repo.auroramc.gg/repository/maven-releases/")
            }
            credentials {
                username = publishing.getProperty("username")
                password = publishing.getProperty("password")
            }
        }
    }

    publications.create<MavenPublication>("mavenJava") {
        groupId = "gg.auroramc"
        artifactId = "AuroraLevels"
        version = project.version.toString()

        from(components["java"])
    }
}