pluginManagement {
	repositories {
		mavenLocal()
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "KikuGie Releases"
			url = uri("https://maven.kikugie.dev/releases")
		}
		maven {
			name = "KikuGie Snapshots"
			url = uri("https://maven.kikugie.dev/snapshots")
		}
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		id("net.fabricmc.fabric-loom") version providers.gradleProperty("loom_version")
		id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
		id("dev.kikugie.loom-back-compat") version "0.3"
		id("org.jetbrains.kotlin.jvm") version providers.gradleProperty("kotlin_version")
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.9.6"
	id("net.fabricmc.fabric-loom") version providers.gradleProperty("loom_version") apply false
	id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version") apply false
	id("dev.kikugie.loom-back-compat") version "0.3"
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

extra["loomx.loom_version"] = providers.gradleProperty("loom_version").get()
extra["loomx.loom_remap_plugin"] = "net.fabricmc.fabric-loom-remap"
extra["loomx.loom_unobf_plugin"] = "net.fabricmc.fabric-loom"

loomx {
	unobfuscated { project ->
		project.name.startsWith("26.")
	}
}

stonecutter {
	create(rootProject) {
		versions("1.16.5", "1.17.1", "1.18.2", "1.19.2", "1.20.1", "1.20.6", "1.21.1", "1.21.11", "26.1.2")
		vcsVersion = "1.21.11"
	}
}

// Should match your modid
rootProject.name = "bedrock-miner"
