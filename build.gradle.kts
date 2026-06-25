import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("dev.kikugie.loom-back-compat")
	`maven-publish`
	id("org.jetbrains.kotlin.jvm")
}

version = "${property("mod.version")}+${sc.current.version}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
	sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
	sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
	sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
	sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
	else -> JavaVersion.VERSION_1_8
}

val requiredJvmTarget = when (requiredJava) {
	JavaVersion.VERSION_25 -> JvmTarget.JVM_25
	JavaVersion.VERSION_21 -> JvmTarget.JVM_21
	JavaVersion.VERSION_17 -> JvmTarget.JVM_17
	JavaVersion.VERSION_16 -> JvmTarget.JVM_16
	JavaVersion.VERSION_1_8 -> JvmTarget.JVM_1_8
	else -> error("Unsupported Java target: $requiredJava")
}

fun scProperty(key: String): String = sc.properties[key]
val processedAccessWidener = sc.process(rootProject.file("src/client/resources/bedrock-miner.accesswidener"), "build/processed.accesswidener")

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
	mavenCentral()
	maven {
		url = uri("https://masa.dy.fi/maven/sakura-ryoko")
	}
	maven {
		url = uri("https://maven.fallenbreath.me/releases")
	}
	maven {
		url = uri("https://maven.terraformersmc.com/releases/")
	}
}

loom {
	splitEnvironmentSourceSets()

	accessWidenerPath = processedAccessWidener

	mods {
		register("bedrock-miner") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets.getByName("client"))
		}
	}
}

dependencies {
	fun fapi(vararg modules: String) {
		for (module in modules) {
			modImplementation(fabricApi.module(module, scProperty("deps.fabric_api")))
		}
	}

	minecraft("com.mojang:minecraft:${sc.current.version}")
	loomx.applyMojangMappings()

	modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

	fapi(
		"fabric-command-api-v2",
		"fabric-events-interaction-v0",
		"fabric-lifecycle-events-v1",
		"fabric-networking-api-v1",
	)
	modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.fabric_kotlin")}")
	modImplementation("fi.dy.masa.malilib:malilib-fabric-${sc.current.version}:${scProperty("deps.malilib")}")
	modCompileOnly("com.terraformersmc:modmenu:${scProperty("deps.mod_menu")}")

	modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

tasks.processResources {
	val props = mapOf(
		"version" to project.version,
		"minecraft" to scProperty("mod.mc_compat"),
		"java" to requiredJava.majorVersion,
		"mixin_java" to "JAVA_${requiredJava.majorVersion}",
	)
	inputs.properties(props)

	filesMatching("fabric.mod.json") {
		expand(props)
	}
	filesMatching("*.mixins.json") {
		expand(props)
	}
}

tasks.named<ProcessResources>("processClientResources") {
	val props = mapOf(
		"version" to project.version,
		"minecraft" to scProperty("mod.mc_compat"),
		"java" to requiredJava.majorVersion,
		"mixin_java" to "JAVA_${requiredJava.majorVersion}",
	)
	inputs.properties(props)

	exclude("bedrock-miner.accesswidener")

	filesMatching("*.mixins.json") {
		expand(props)
	}

	from(processedAccessWidener) {
		rename { "bedrock-miner.accesswidener" }
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = requiredJava.majorVersion.toInt()
}

kotlin {
	compilerOptions {
		jvmTarget = requiredJvmTarget
	}
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = requiredJava
	targetCompatibility = requiredJava
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
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
