package com.github.lxyan2333.bedrockminer.config

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

object ServerConfigManager {
    private val configFile = FabricLoader.getInstance().configDir.resolve("bedrock-miner-server.json")

    fun load() {
        if (!Files.exists(configFile)) return
        try {
            val element = JsonParser.parseReader(Files.newBufferedReader(configFile))
            if (element.isJsonObject) {
                val root = element.asJsonObject
                if (root.has("blockList")) {
                    ServerConfigData.serverBlockList = root.getAsJsonArray("blockList").map { it.asString }.toMutableSet()
                }
                if (root.has("allowList")) {
                    ServerConfigData.serverAllowList = root.getAsJsonArray("allowList").map { it.asString }.toMutableSet()
                }
                if (root.has("blockListMode")) {
                    ServerConfigData.serverBlockListMode = root.get("blockListMode").asString
                }
            }
        } catch (_: Exception) {
        }
    }

    fun save() {
        try {
            val dir = configFile.parent
            if (!Files.exists(dir)) {
                Files.createDirectories(dir)
            }
            val root = JsonObject()
            val blockListArr = com.google.gson.JsonArray()
            ServerConfigData.serverBlockList.forEach { blockListArr.add(it) }
            root.add("blockList", blockListArr)

            val allowListArr = com.google.gson.JsonArray()
            ServerConfigData.serverAllowList.forEach { allowListArr.add(it) }
            root.add("allowList", allowListArr)

            root.addProperty("blockListMode", ServerConfigData.serverBlockListMode)
            Files.newBufferedWriter(configFile).use { writer ->
                com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root, writer)
            }
        } catch (_: Exception) {
        }
    }
}
