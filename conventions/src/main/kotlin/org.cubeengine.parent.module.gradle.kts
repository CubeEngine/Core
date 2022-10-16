import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream

plugins {
    `java-library`
    `maven-publish`
    id("org.cadixdev.licenser")
    id("com.github.johnrengelman.shadow")
}

val pluginGroupId: String by project.properties
val pluginDescription: String by project.properties
val pluginVersion: String by project.properties
val spongeVersion: String by project.properties
val pluginIsSnapshot: String by project.properties
val moduleId: String by project.properties
val moduleName: String by project.properties

val spongeMajorVersion: String = spongeVersion.substring(0, spongeVersion.indexOf('.'))
val snapshotVersion = if (pluginIsSnapshot.toBoolean()) "-SNAPSHOT" else ""

group = pluginGroupId
version = "$spongeMajorVersion.$pluginVersion$snapshotVersion"
description = pluginDescription

val releasesRepoUrl = uri("https://maven.cubyte.org/repository/releases/")
val snapshotsRepoUrl = uri("https://maven.cubyte.org/repository/snapshots/")

// repos for modules **using** this convention
repositories {
    mavenCentral()
    maven(releasesRepoUrl)
    maven(snapshotsRepoUrl)
    mavenLocal()
}

dependencies {
    // sponge
    compileOnly("org.spongepowered:spongeapi:$spongeVersion")

    annotationProcessor("org.cubeengine:plugin-gen")

    // Testing
    val junitVersion = "5.9.1"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.slf4j:slf4j-simple:2.0.3")
    testImplementation("org.spongepowered:spongeapi:$spongeVersion")

    // LibCube Plugin Dependency
    val libCubeVersion = project.properties["libCubeVersion"]
    if (libCubeVersion != null) {
        implementation("org.cubeengine:libcube:$libCubeVersion")
    }

    constraints {
        val pluginGenVersion = "1.0.7-SNAPSHOT"
        annotationProcessor("org.cubeengine:plugin-gen:$pluginGenVersion")
        implementation("org.cubeengine:plugin-gen:$pluginGenVersion")
    }
}

tasks.test {
    useJUnitPlatform()
}

fun getGitCommit(): String? {
    return try {
        val byteOut = ByteArrayOutputStream()
        project.exec {
            commandLine = "git rev-parse HEAD".split(" ")
            standardOutput = byteOut
        }
        byteOut.toString("UTF-8").trim()
    } catch (e: Exception) {
        // ignore
        null
    }
}

val orgName = "CubeEngine"
val orgUrl = "https://cubeengine.org"

fun annotationProcessorArg(name: String, value: Any?) = value?.let { "-A$name=$it" }
fun pluginGenArg(name: String, value: Any?) = annotationProcessorArg("cubeengine.module.$name", value)

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOfNotNull(
            pluginGenArg("version", project.version),
            pluginGenArg("sourceversion", getGitCommit()),
            pluginGenArg("description", project.description),
            pluginGenArg("id", moduleId),
            pluginGenArg("name", moduleName),
            pluginGenArg("team", orgName),
            pluginGenArg("url", orgUrl),
            pluginGenArg("libcube.version", project.properties["libCubeVersion"]),
            pluginGenArg("sponge.version", spongeVersion),
        )
    )
}

val projectJvmTarget = "17"
java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(projectJvmTarget))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

project.gradle.projectsEvaluated {
    publishing {
        repositories {
            maven {
                name = "cubyte"
                url = if (project.version.toString().endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials(PasswordCredentials::class)
            }
        }
    }
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("")
}

publishing {
    publications {
        publications.create<MavenPublication>("cubyte") {
            project.shadow.component(this)
            artifact(tasks.getByName("sourcesJar"))
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://cubeengine.org")
                licenses {
                    license {
                        name.set("GNU General Public License Version 3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("pschichtel")
                        name.set("Phillip Schichtel")
                        email.set("phillip@schich.tel")
                    }
                    developer {
                        id.set("faithcaio")
                        name.set("Anselm Brehme")
                    }
                    developer {
                        id.set("boeserwolf91")
                        name.set("Stefan Wolf")
                    }
                    developer {
                        id.set("totokaka")
                        name.set("Tobias Laundal")
                    }
                }
                scm {
                    url.set("https://github.com/CubeEngine/core")
                    connection.set("scm:git:https://github.com/CubeEngine/core")
                    developerConnection.set("scm:git:git@github.com:CubeEngine/core")
                }
            }
        }
    }
}

tasks.classes.configure {
    dependsOn(tasks.licenseFormat)
}

license {
    // TODO change to master branch once it's merged
    header(project.resources.text.fromUri("https://raw.githubusercontent.com/CubeEngine/core/feature/gradle/header.txt"))
    newLine(false)
    exclude("**/*.info")
    exclude("assets/**")
    exclude("*.kts")
    exclude("**/*.json")
    exclude("**/*.properties")
    exclude("*.txt")
}
