import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

//val postgresVersion = "42.3.1"
//val telegramBotVersion = "5.3.0"

plugins {
//    id("nu.studer.jooq") version("6.0.1")
//    id("org.flywaydb.flyway") version("7.7.0")
//    id("org.springframework.boot") version "2.5.6"
//    id("io.spring.dependency-management") version "1.0.11.RELEASE"
//    kotlin("plugin.spring") version "1.5.31"
    id("java")
    kotlin("jvm") version "2.0.21"
//    id("org.jetbrains.kotlin.jvm") version "2.0.21"
//    id("com.github.johnrengelman.shadow") version "8.1.1"
//    id("com.gradleup.shadow") version "8.3.4"
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
//    maven ("https://dl.bintray.com/palantir/releases")
    mavenCentral()
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

//    implementation("jdbc:jdbc:2.0")

    // Storage
    // LevelDB database
    include(implementation("org.iq80.leveldb:leveldb:${project.property("leveldb_version")}")) {}
    include(implementation("org.iq80.leveldb:leveldb-api:${project.property("leveldb_version")}")) {}

    // MongoDB driver
    include(implementation("org.mongodb:mongodb-driver-sync:${project.property("mongodb_version")}")) {}
    include(implementation("org.mongodb:mongodb-driver-core:${project.property("mongodb_version")}")) {}
    include(implementation("org.mongodb:bson:${project.property("mongodb_version")}")) {}

    // MySQL driver
    include(implementation("com.mysql:mysql-connector-j:${project.property("mysql_version")}")) {}

    include(implementation("com.squareup.retrofit2:retrofit:2.11.0") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-serialization-core")
        exclude(module = "kotlinx-serialization-json")
    })
    include(implementation("com.squareup.retrofit2:converter-gson:2.11.0") {
        exclude(module = "gson")
    })
    include(implementation("com.charleskorn.kaml:kaml:${kamlVersion}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-serialization-core")
    })
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
//    named<ShadowJar>("shadowJar") {
//        dependsOn("processResources")
//        finalizedBy("remapJar")
//
//        from(sourceSets.main.get().output.classesDirs)
//        from(sourceSets.main.get().output.resourcesDir)
//
//        from("LICENSE") {
//            rename { "${it}_${project.base.archivesName.get()}" }
//        }
//        relocate("okio", "zixamc.requests.shaded.okio")
//        relocate("okhttp3", "zixamc.requests.shaded.okhttp3")
//        relocate("retrofit2", "zixamc.requests.shaded.retrofit2")
//
//        relocate("it.krzeminski.snakeyaml", "zixamc.requests.shaded.snakeyaml")
//        relocate("net.thauvin", "zixamc.requests.shaded.net.thauvin")
//        relocate("com.charleskorn.kaml", "zixamc.requests.shaded.kaml")
//        mergeServiceFiles()
//        minimize()
//
//        configurations = listOf(project.configurations.shadow.get())
//        archiveClassifier = jar.get().archiveClassifier
//        destinationDirectory = jar.get().destinationDirectory
//    }
//    named<RemapJarTask>("remapJar") {
//        inputFile = shadowJar.get().archiveFile
//        archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
//        destinationDirectory.set(file("../build/release"))
//    }
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
//tasks.shadowJar {
//    configurations = [project.configurations.shadow]
//    exclude("META-INF")
//}
tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
    manifest {
        attributes["Main-Class"] = "ru.kochkaev.zixamc.requests.ZixaMCRequests"
    }
}
//tasks {
//    named<ShadowJar>("shadowJar") {
//        dependsOn("processResources")
//        finalizedBy("remapJar")
//        from("LICENSE") {
//            rename { "${it}_${project.base.archivesName}" }
//        }
//        relocate("okio", "tgbridge.shaded.okio")
//        relocate("okhttp3", "tgbridge.shaded.okhttp3")
//        relocate("retrofit2", "tgbridge.shaded.retrofit2")
//        relocate("com.charleskorn.kaml", "tgbridge.shaded.kaml")
//        relocate("mongodb", "tgbridge.shaded.mongodb")
//        relocate("mysql", "tgbridge.shaded.mysql")
//        relocate("leveldb", "tgbridge.shaded.leveldb")
//
//        archiveBaseName.set(project.property("archives_base_name") as String)
//        archiveClassifier.set("all")
//        archiveVersion.set(version as String)
//        mergeServiceFiles()
//
//        from(sourceSets.main.get().output.classesDirs)
//        from(sourceSets.main.get().output.resourcesDir)
////        configurations = listOf(project.configurations.shadow.get()
////            .filter {
////                it.name.contains("retrofit") || it.name.contains("okhttp") || it.name.contains("okio") || it.name.contains("kaml")
////                    || it.name.contains("mongodb") || it.name.contains("leveldb") || it.name.contains("mysql")
////            }
////        )
//
//
////        dependencies {
////            exclude(dependency("com.mojang:minecraft:.*"))
////            exclude(dependency("net.fabricmc:yarn:.*"))
////            exclude(dependency("net.fabricmc:fabric-loader:.*"))
////            exclude(dependency("net.fabricmc:fabric-language-kotlin:.*"))
////            exclude(dependency("net.fabricmc.fabric-api:.*"))
////        }
////        exclude("META-INF")
////        manifest {
////            attributes["Main-Class"] = "ru.kochkaev.zixamc.requests.ZixaMCRequests"
////        }
//        minimize()
//
//    }
//    named<RemapJarTask>("remapJar") {
//		inputFile = shadowJar.get().archiveFile
//		archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
//		destinationDirectory.set(file("../build/release"))
//	}
//}
//tasks.withType<ShadowJar> {
//
//}

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
