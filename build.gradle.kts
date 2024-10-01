import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.core") version "1.7.3"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
    }
    //tasks.withType<Javadoc> {
    //    options.encoding = Charsets.UTF_8.name()
    //}
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }
}

val spigotDecompiler: Configuration by configurations.creating

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content {
            onlyForConfigurations(
                configurations.paperclip.name,
                spigotDecompiler.name,
            )
        }
    }
}

dependencies {
    paramMappings("net.fabricmc:yarn:1.21.1+build.3:mergedv2")
    remapper("net.fabricmc:tiny-remapper:0.10.3:fat")
    decompiler("org.vineflower:vineflower:1.10.1")
    spigotDecompiler("io.papermc:patched-spigot-fernflower:0.1+build.13")
    paperclip("io.papermc:paperclip:3.0.3")
}

paperweight {
    minecraftVersion = providers.gradleProperty("mcVersion")
    serverProject = project(":paper-server")

    paramMappingsRepo = paperMavenPublicUrl
    remapRepo = paperMavenPublicUrl
    decompileRepo = paperMavenPublicUrl

    craftBukkit {
        fernFlowerJar = layout.file(spigotDecompiler.elements.map { it.single().asFile })
    }

    paper {
        spigotApiPatchDir = layout.projectDirectory.dir("patches/api")
        spigotServerPatchDir = layout.projectDirectory.dir("patches/server")

        mappingsPatch = layout.projectDirectory.file("build-data/mappings-patch.tiny")
        reobfMappingsPatch = layout.projectDirectory.file("build-data/reobf-mappings-patch.tiny")

        reobfPackagesToFix.addAll(
            "co.aikar.timings",
            "com.destroystokyo.paper",
            "com.mojang",
            "io.papermc.paper",
            "ca.spottedleaf",
            "net.kyori.adventure.bossbar",
            "net.minecraft",
            "org.bukkit.craftbukkit",
            "org.spigotmc",
        )
    }
}

tasks.generateDevelopmentBundle {
    apiCoordinates = "io.papermc.paper:paper-api"
    libraryRepositories.addAll(
        "https://repo.maven.apache.org/maven2/",
        paperMavenPublicUrl,
    )
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printPaperVersion") {
    doLast {
        println(project.version)
    }
}
