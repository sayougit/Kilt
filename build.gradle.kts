import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.daemon.common.toHexString
import xyz.bluspring.kilt.gradle.AccessTransformerRemapper
import java.security.MessageDigest

plugins {
    kotlin("jvm")
    id ("fabric-loom") version "1.9-SNAPSHOT"
    id ("maven-publish")
    id ("org.ajoberstar.grgit") version "5.0.0" apply false
}

version = "${property("mod_version")}+mc${property("minecraft_version")}${getVersionMetadata()}"
group = property("maven_group")!!

base {
    archivesName.set(property("archives_base_name")!! as String)
}

sourceSets {
    getByName("main") {
        java.srcDir("src/main/java")
        java.srcDir("src/main/kotlin")
        java.srcDir("forge/src/main/java")

        resources.srcDir("forge/src/generated/resources")
        resources.srcDir("forge/src/main/resources")
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/kilt.accesswidener"))
    mixin {
        showMessageTypes.set(true)

        messages.set(mutableMapOf("ACCESSOR_TARGET_NOT_FOUND" to "disabled"))
    }
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://mvn.devos.one/releases/") {
        name = "devOS Maven"
    }

    maven("https://mvn.devos.one/snapshots/") {
        name = "devOS Maven (Snapshots)"
    }

    maven("https://jitpack.io/") {
        name = "JitPack"
    }

    maven("https://maven.cafeteria.dev/releases/") {
        name = "Cafeteria Dev"
    }

    maven("https://maven.jamieswhiteshirt.com/libs-release") {
        name = "JamiesWhiteShirt Dev"
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }

    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") {
        name = "Fuzs Mod Resources"
    }

    maven("https://maven.minecraftforge.net/") {
        name = "MinecraftForge Maven"
    }

    maven("https://maven.architectury.dev") {
        name = "Architectury"
    }

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }

    flatDir {
        dir("libs")
    }

    // Testing mod sources
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://cursemaven.com") {
        name = "CurseMaven"
        content {
            includeGroup("curse.maven")
        }
    }

    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }

    maven("https://maven.su5ed.dev/releases") {
        name = "Su5ed"
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft ("com.mojang:minecraft:${property("minecraft_version")}")
    mappings (loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}:${property("parchment_release")}@zip")
    })
    modImplementation ("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation ("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Just because I like Kotlin more than Java
    modImplementation ("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    // we require Indium due to us using Fabric Rendering API stuff.
    // let's tell the users that too.
    modImplementation(include("me.luligabi:NoIndium:${property("no_indium_version")}") {
        exclude("net.fabricmc", "fabric-loader")
    })

    // Forge Reimplementations
    val portingLibs = listOf("accessors", "asm", "attributes", "base", "blocks", "brewing", "chunk_loading", "client_events", "common", "core", "data", "entity", "extensions", "fluids", "gametest", "gui_utils", "items", "lazy_registration", "level_events", "loot", "mixin_extensions", "model_builders", "model_generators", "model_loader", "model_materials", "models", "networking", "obj_loader", "recipe_book_categories", "registries", "tags", "tool_actions", "transfer", "utility")
    portingLibs.forEach { lib ->
        modImplementation(include("io.github.fabricators_of_create.Porting-Lib:$lib:${property("porting_lib_version")}")!!)
    }
    modImplementation ("dev.architectury:architectury-fabric:${property("architectury_version")}")

    // Cursed Fabric/Mixin stuff
    implementation(include("com.github.FabricCompatibilityLayers:CursedMixinExtensions:${property("cursedmixinextensions_version")}")!!)
    modImplementation(include("com.github.Chocohead:Fabric-ASM:v${property("fabric_asm_version")}")!!)
    include(implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${property("mixin_squared_version")}")!!)!!)
    include(modImplementation("de.florianmichael:AsmFabricLoader:${property("asmfabricloader_version")}")!!)

    // TODO: remove this when 0.5 is mainlined into Fabric
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${property("mixinextras_version")}")!!)!!)

//modImplementation(include("io.github.tropheusj:serialization-hooks:${property("serialization_hooks_version")}")!!)
    modImplementation(include("com.jamieswhiteshirt:reach-entity-attributes:${property("reach_entity_attributes_version")}")!!)
    modImplementation("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${property("forgeconfigapiport_version")}")
    include(implementation("xyz.bluspring.kiltmc:MixinConstraints:${property("mixinconstraints_version")}") {
        exclude("org.spongepowered", "mixin")
    })

    // Forge stuff
    implementation(include("xyz.bluspring:eventbus:${property("eventbus_version")}") {
        exclude("cpw.mods", "modlauncher")
        exclude("net.minecraftforge", "modlauncher")
        exclude("net.minecraftforge", "securemodules")
    })
    implementation(include("net.minecraftforge:forgespi:${property("forgespi_version")}") {
        exclude("cpw.mods", "modlauncher")
        exclude("net.minecraftforge", "modlauncher")
        exclude("net.minecraftforge", "securemodules")
    })
    implementation(include("org.apache.maven:maven-artifact:3.8.5")!!)
    implementation(include("cpw.mods:securejarhandler:${property("securejarhandler_version")}")!!)
    implementation(include("net.jodah:typetools:0.8.3")!!)
    implementation(include("net.minecraftforge:unsafe:0.2.+")!!)
    implementation(include("net.minecraftforge:mergetool-api:1.0")!!)
    implementation(include("org.jline:jline-reader:3.12.+")!!)
    implementation(include("net.minecrell:terminalconsoleappender:1.3.0")!!)
    implementation(include("org.openjdk.nashorn:nashorn-core:${property("nashorn_version")}")!!) // for CoreMods

    // Remapping SRG to Intermediary
    implementation(include("net.minecraftforge:srgutils:0.4.13")!!)
    implementation(include("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")!!)

    modImplementation(include("teamreborn:energy:${property("teamreborn_energy_version")}")!!)

    // Use Kilt's fork of Sinytra Connector's fork of ForgeAutoRenamingTool
    implementation(include("xyz.bluspring:AutoRenamingTool:${property("forgerenamer_version")}")!!)

    fun modOptional(dependencyNotation: String, shouldRunInRuntime: Boolean, configuration: Action<ExternalModuleDependency>) {
        if (shouldRunInRuntime) {
            modImplementation(dependencyNotation, configuration)
        } else {
            modCompileOnly(dependencyNotation, configuration)
        }
    }

    val runSodium = true

    // Runtime mods for testing
    modRuntimeOnly ("com.terraformersmc:modmenu:7.1.0") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:ferrite-core:6.0.1-fabric") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modOptional ("maven.modrinth:sodium:mc1.20.1-0.5.11", runSodium) {
        exclude("net.fabricmc", "fabric-loader")
    }
    modRuntimeOnly ("maven.modrinth:lithium:mc1.20.1-0.11.2") {
        exclude("net.fabricmc", "fabric-loader")
    }
    modOptional ("maven.modrinth:indium:1.0.34+mc1.20.1", runSodium) {
        exclude("net.fabricmc", "fabric-loader")
    }

    // apparently I need this for Nullable to exist
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    implementation(include("commons-codec:commons-codec:1.15")!!)
}

configurations.all {
    exclude("cpw.mods", "modlauncher")
}

val targetJavaVersion = "17"

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }

    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks {
    register("countPatchProgress") {
        group = "kilt"
        description = "Counts the total of patches in Forge, and checks how many Kilt ForgeInjects there are, to check how much is remaining."

        doFirst {
            // Scan Forge patches dir
            var count = 0

            fun readDir(file: File) {
                val files = file.listFiles()!!

                files.forEach {
                    if (it.isDirectory) {
                        readDir(it)
                    } else {
                        count++
                    }
                }
            }

            readDir(File("$projectDir/forge/patches"))

            val forgePatchCount = count
            count = 0

            readDir(File("$projectDir/src/main/java/xyz/bluspring/kilt/forgeinjects"))
            val kiltInjectCount = count

            println("Progress: $kiltInjectCount injects/$forgePatchCount patches (${String.format("%.2f", (kiltInjectCount.toDouble() / forgePatchCount.toDouble()) * 100.0)}%)")
        }
    }

    register("tagPatches") {
        group = "kilt"
        description = "Tags the Kilt ForgeInjects with their currently tracked patch hash to ensure they are all up to date."

        doFirst {
            fun readDir(file: File) {
                val files = file.listFiles()!!
                val md = MessageDigest.getInstance("SHA1")

                files.forEach {
                    if (it.isDirectory) {
                        readDir(it)
                    } else {
                        val startDir = it.absolutePath.replace("\\", "/").replaceBefore("forgeinjects/", "").replace("forgeinjects/", "")
                        val patchDir = if (startDir.startsWith("blaze3d") || startDir.startsWith("math")) "com/mojang/${startDir.replace("Inject.java", ".java.patch")}"
                            else "net/minecraft/${startDir.replace("Inject.java", ".java.patch")}"

                        val patchFile = File("$projectDir/forge/patches/minecraft/$patchDir")
                        if (!patchFile.exists()) {
                            println("!! WARNING !! Inject $startDir no longer has an associated patch file!")
                            return@forEach
                        }

                        val patchHash = md.digest(patchFile.readBytes()).toHexString()

                        val data = it.readLines().toMutableList()
                        if (!data[0].startsWith("// TRACKED HASH: ")) {
                            data.add(0, "// TRACKED HASH: $patchHash")
                            it.writeText(data.joinToString("\r\n"))
                        } else {
                            val oldHash = data[0].removePrefix("// TRACKED HASH: ")

                            if (oldHash != patchHash) {
                                println("Inject $startDir is outdated! (patch: $patchHash, inject: $oldHash) Updating hash...")
                                data[0] = "// TRACKED HASH: $patchHash"
                                it.writeText(data.joinToString("\r\n"))
                            }
                        }
                    }
                }
            }

            readDir(File("$projectDir/src/main/java/xyz/bluspring/kilt/forgeinjects"))
        }
    }

    processResources {
        inputs.property("version", project.version)
        inputs.property("loader_version", project.property("loader_version"))
        inputs.property("fabric_version", project.property("fabric_version"))
        inputs.property("minecraft_version", project.property("minecraft_version"))
        inputs.property("fabric_kotlin_version", project.property("fabric_kotlin_version"))
        inputs.property("fabric_asm_version", project.property("fabric_asm_version"))
        inputs.property("forge_config_version", project.property("forgeconfigapiport_version"))
        inputs.property("architectury_version", project.property("architectury_version"))
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(mutableMapOf(
                "version" to project.version,
                "loader_version" to project.property("loader_version"),
                "fabric_version" to project.property("fabric_version"),
                "minecraft_version" to project.property("minecraft_version"),
                "fabric_kotlin_version" to project.property("fabric_kotlin_version"),
                "fabric_asm_version" to project.property("fabric_asm_version"),
                "forge_config_version" to project.property("forgeconfigapiport_version"),
                "architectury_version" to project.property("architectury_version"),
            ))
        }

        // Rename Forge's mods.toml, so launchers like Prism don't end up detecting it over Kilt.
        filesMatching("META-INF/mods.toml") {
            this.name = "forge.mods.toml"
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = targetJavaVersion
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${archiveBaseName.get()}" }
        }
    }

    named<Jar>("sourcesJar") {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    // configure the maven publication
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
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

    register("setupDevEnvironment") {
        group = "kilt"

        doLast {
            val configDir = File("$projectDir/run/config")
            if (!configDir.exists())
                configDir.mkdirs()

            val loaderDepsFile = File(configDir, "fabric_loader_dependencies.json")

            if (!loaderDepsFile.exists())
                loaderDepsFile.createNewFile()

            loaderDepsFile.writeText(File("$projectDir/gradle/loader_dep_overrides.json").readText())
        }
    }

    register("transformerToWidener") {
        group = "kilt"

        doLast {
            val remapper = AccessTransformerRemapper()
            val transformerFile = File("$projectDir/forge/src/main/resources/META-INF/accesstransformer.cfg")
            val widenerFile = File("$projectDir/src/main/resources/kilt.accesswidener")

            remapper.convertTransformerToWidener(
                transformerFile.readText(),
                widenerFile,
                project.property("minecraft_version") as String,
                layout.buildDirectory.get().asFile
            )
        }
    }
}

fun getVersionMetadata(): String {
    val grgit = Grgit.open(mutableMapOf<String, Any?>(
        "dir" to File("$projectDir")
    ))
    val commitHash =
        System.getenv("GITHUB_SHA") ?: grgit.head().abbreviatedId

    return "+build.${commitHash.subSequence(0, 6)}${if (System.getenv("GITHUB_RUN_NUMBER") == null) "-local" else ""}"
}