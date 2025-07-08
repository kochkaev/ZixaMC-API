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
    kotlin("jvm") version "2.2.0"
//    id("org.jetbrains.kotlin.jvm") version "2.0.21"
//    id("com.github.johnrengelman.shadow") version "8.1.1"
//    id("com.gradleup.shadow") version "8.3.4"
    id("fabric-loom") version "1.10.1"
//    id("fabric-loom") version "1.7.1"
    id("maven-publish")
}

val kamlVersion = "0.56.0"
val tgbridgeVersion = "0.5.0"
val adventureVersion: String by project
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

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "fabric-loom")

    if (project.name == "main") {
        loom {
            splitEnvironmentSourceSets()

            mods {
                register("zixamcrequests") {
                    sourceSet("main")
                    sourceSet("client")
                }
            }
        }
    } else {
        tasks {
            runServer {
                enabled = false
            }
            runClient {
                enabled = false
            }
        }
    }
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    exclusiveContent {
        forRepository {
            maven ("https://api.modrinth.com/maven") { name = "Modrinth" }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://jitpack.io")
    maven("https://masa.dy.fi/maven")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
//    maven ("https://dl.bintray.com/palantir/releases")
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:${project.property("loader_version")}")
        force("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    compileOnly(project(":stubs"))
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

    include(implementation("com.squareup.okhttp3:okhttp:4.12.0")) {}
    include(implementation("com.squareup.okio:okio-jvm:3.6.0")) {}
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

    // EasyAuth
    compileOnly("maven.modrinth:easyauth:${project.property("easyauth_version")}")
//    compileOnly("maven.modrinth:easywhitelist:1.0.1")
//    modImplementation("xyz.nucleoid:server-translations-api:2.4.0+1.21.2-rc1")
    modImplementation("xyz.nucleoid:server-translations-api:${project.property("server_translations_api_version")}")
    // Password hashing
    // Argon2
    modImplementation("de.mkammerer:argon2-jvm:2.11")
    modImplementation("de.mkammerer:argon2-jvm-nolibs:2.11")
    // BCrypt
    modImplementation("at.favre.lib:bcrypt:0.10.2")
    modImplementation("at.favre.lib:bytes:1.6.1")
    // Hocon config
    modImplementation("org.spongepowered:configurate-core:4.1.2")
    modImplementation("org.spongepowered:configurate-hocon:4.1.2")
    modImplementation("org.apache.commons:commons-text:1.10.0")
    modImplementation("com.typesafe:config:1.4.3")
    modImplementation("io.leangen.geantyref:geantyref:1.3.13")
    // JNA lib
    modImplementation("net.java.dev.jna:jna:5.13.0")

    include(implementation("net.kyori:adventure-api:${adventureVersion}")!!)
    include(implementation("net.kyori:adventure-key:${adventureVersion}")!!)
    include(implementation("net.kyori:adventure-text-serializer-gson:${adventureVersion}") {
        exclude(module = "gson")
    })
    include(implementation("net.kyori:adventure-text-serializer-json:${adventureVersion}") {
        exclude(module = "gson")
    })
    include(implementation("net.kyori:adventure-text-minimessage:${adventureVersion}")!!)
    include(implementation("net.kyori:examination-api:1.3.0")!!)
    include(implementation("net.kyori:examination-string:1.3.0")!!)
    include(implementation("net.kyori:option:1.0.0")!!)

    compileOnly("com.google.code.gson:gson:2.10.1")
    modImplementation("maven.modrinth:vanish:${project.property("vanish_version")}")
    modImplementation("eu.pb4:player-data-api:${project.property("player_data_api_version")}")
    modImplementation("me.lucko:fabric-permissions-api:${project.property("fabric_permissions_api_version")}")
    modImplementation("eu.pb4:predicate-api:${project.property("predicate_api_version")}")
    modImplementation("eu.pb4:placeholder-api:${project.property("placeholder_api_version")}")

    modImplementation("maven.modrinth:audioplayer:${project.property("audioplayer_version")}")
//
//    modImplementation("maven.modrinth:fabrictailor:2.5.0") {
//        exclude(group = "net.fabricmc.fabric-loader")
//        exclude(group = "net.fabricmc.fabric-api")
//    }
//    modImplementation("com.github.samolego.Config2Brigadier:config2brigadier-common:1.2.3") {
//        exclude(group = "net.fabricmc.fabric-loader")
//        exclude(group = "net.fabricmc.fabric-api")
//    }
//    modImplementation("me.lucko:fabric-permissions-api:0.3.3") {
//        exclude(group = "net.fabricmc.fabric-loader")
//        exclude(group = "net.fabricmc.fabric-api")
//    }

    // Markdown
//    include(implementation("org.commonmark:commonmark:0.18.2")!!)
//    include(implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.18.2")!!)
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
//        relocate("okio", "zixamc.tgbridge.shaded.okio")
//        relocate("okhttp3", "zixamc.tgbridge.shaded.okhttp3")
//        relocate("retrofit2", "zixamc.tgbridge.shaded.retrofit2")
//
//        relocate("it.krzeminski.snakeyaml", "zixamc.tgbridge.shaded.snakeyaml")
//        relocate("net.thauvin", "zixamc.tgbridge.shaded.net.thauvin")
//        relocate("com.charleskorn.kaml", "zixamc.tgbridge.shaded.kaml")
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
        attributes["Main-Class"] = "ru.kochkaev.zixamc.api.ZixaMC"
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
////            attributes["Main-Class"] = "ru.kochkaev.zixamc.tgbridge.ZixaMCRequests"
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
