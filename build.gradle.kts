import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//val postgresVersion = "42.3.1"
//val telegramBotVersion = "5.3.0"

plugins {
//    id("nu.studer.jooq") version("6.0.1")
//    id("org.flywaydb.flyway") version("7.7.0")
//    id("org.springframework.boot") version "2.5.6"
//    id("io.spring.dependency-management") version "1.0.11.RELEASE"
//    kotlin("plugin.spring") version "1.5.31"
    kotlin("jvm") version "2.0.21"
    id("fabric-loom") version "1.7.1"
    id("maven-publish")
}

val kamlVersion = "0.56.0"
val tgbridgeVersion = "0.5.0"
version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("zixamcrequests") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    exclusiveContent {
        forRepository {
            maven ("https://api.modrinth.com/maven") { name = "Modrinth" }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
//    modImplementation("maven.modrinth:tgbridge:$tgbridgeVersion")

//    implementation("org.springframework.boot:spring-boot-starter")
//    implementation("org.springframework.boot:spring-boot-starter-jooq")
//    implementation("org.springframework.boot:spring-boot-starter-freemarker")
//    implementation("org.springframework.boot:spring-boot-starter-web")
//
//    implementation("org.telegram:telegrambots:$telegramBotVersion")
//    implementation("org.telegram:telegrambotsextensions:$telegramBotVersion")
//    implementation("org.telegram:telegrambots-spring-boot-starter:$telegramBotVersion")

    implementation("com.squareup.retrofit2:retrofit:2.11.0") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-serialization-core")
        exclude(module = "kotlinx-serialization-json")
    }
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") {
        exclude(module = "gson")
    }
    implementation("com.charleskorn.kaml:kaml:${kamlVersion}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-serialization-core")
    }
    compileOnly("com.google.code.gson:gson:2.10.1")
}
//dependencyManagement {
//    imports {
//        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
//    }
//}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "kotlin_loader_version" to project.property("kotlin_loader_version")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
