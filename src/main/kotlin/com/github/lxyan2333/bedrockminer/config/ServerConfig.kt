package com.github.lxyan2333.bedrockminer.config

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

object ServerConfig {
    const val PROTOCOL_VERSION = 1

    val SPECIAL_BLOCKS = setOf(
        "minecraft:barrier",
        "minecraft:command_block",
        "minecraft:chain_command_block",
        "minecraft:repeating_command_block",
        "minecraft:structure_block",
        "minecraft:structure_void",
        "minecraft:jigsaw",
    )

    var serverHasMod = false
        private set

    var serverProtocolVersion = -1
        private set

    var serverBlockList: List<String> = SPECIAL_BLOCKS.toList()
        private set

    var serverAllowList: List<String> = listOf()
        private set

    var serverBlockListMode: String = "ALLOWED"
        private set

    var onConfigReceived: (() -> Unit)? = null

    private val configFile = FabricLoader.getInstance().configDir.resolve("bedrock-miner-server.json")

    fun load() {
        if (!Files.exists(configFile)) return
        try {
            val element = JsonParser.parseReader(Files.newBufferedReader(configFile))
            if (element.isJsonObject) {
                val root = element.asJsonObject
                if (root.has("blockList")) {
                    serverBlockList = root.getAsJsonArray("blockList").map { it.asString }
                }
                if (root.has("allowList")) {
                    serverAllowList = root.getAsJsonArray("allowList").map { it.asString }
                }
                if (root.has("blockListMode")) {
                    serverBlockListMode = root.get("blockListMode").asString
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
            serverBlockList.forEach { blockListArr.add(it) }
            root.add("blockList", blockListArr)

            val allowListArr = com.google.gson.JsonArray()
            serverAllowList.forEach { allowListArr.add(it) }
            root.add("allowList", allowListArr)

            root.addProperty("blockListMode", serverBlockListMode)
            Files.newBufferedWriter(configFile).use { writer ->
                com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root, writer)
            }
        } catch (_: Exception) {
        }
    }

    fun applyFromPacket(protocolVersion: Int, blockList: List<String>, allowList: List<String>, blockListMode: String) {
        serverHasMod = true
        serverProtocolVersion = protocolVersion
        serverBlockList = blockList
        serverAllowList = allowList
        serverBlockListMode = blockListMode
        onConfigReceived?.invoke()
    }

    fun resetOnDisconnect() {
        serverHasMod = false
        serverProtocolVersion = -1
        serverBlockList = listOf()
        serverAllowList = listOf()
        serverBlockListMode = "BLOCKED"
    }
}
