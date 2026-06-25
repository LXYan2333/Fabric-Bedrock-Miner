package com.github.lxyan2333.bedrockminer.config

import com.github.lxyan2333.bedrockminer.compat.NetworkCompat
import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.server.MinecraftServer
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

object ServerConfigManager {
    private val configFile = FabricLoader.getInstance().configDir.resolve("bedrock-miner-server.json")

    lateinit var server: MinecraftServer
        private set

    fun init(server: MinecraftServer) {
        this.server = server
    }

    fun load() {
        if (!Files.exists(configFile)) return
        try {
            val element = JsonParser.parseReader(Files.newBufferedReader(configFile))
            if (element.isJsonObject) {
                val root = element.asJsonObject
                if (root.has("blockList")) {
                    ServerConfigData.serverBlockList =
                        root.getAsJsonArray("blockList").map { it.asString }.toMutableSet()
                }
                if (root.has("allowList")) {
                    ServerConfigData.serverAllowList =
                        root.getAsJsonArray("allowList").map { it.asString }.toMutableSet()
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

    fun isValidBlockName(name: String): Boolean {
        return IdentifierCompat.isKnownBlock(name)
    }

    fun addToBlockList(block: String): Boolean {
        if (!isValidBlockName(block)) return false
        ServerConfigData.serverBlockList.add(block)
        save()
        broadcastConfig()
        return true
    }

    fun removeFromBlockList(block: String): Boolean {
        if (!ServerConfigData.serverBlockList.remove(block)) return false
        save()
        broadcastConfig()
        return true
    }

    fun clearBlockList() {
        ServerConfigData.serverBlockList.clear()
        save()
        broadcastConfig()
    }

    fun addToAllowList(block: String): Boolean {
        if (!isValidBlockName(block)) return false
        ServerConfigData.serverAllowList.add(block)
        save()
        broadcastConfig()
        return true
    }

    fun removeFromAllowList(block: String): Boolean {
        if (!ServerConfigData.serverAllowList.remove(block)) return false
        save()
        broadcastConfig()
        return true
    }

    fun clearAllowList() {
        ServerConfigData.serverAllowList.clear()
        save()
        broadcastConfig()
    }

    fun setBlockListMode(mode: String) {
        ServerConfigData.serverBlockListMode = mode
        save()
        broadcastConfig()
    }

    fun broadcastConfig() {
        val payload = ModNetwork.ConfigSyncPayload(
            ServerConfigData.PROTOCOL_VERSION,
            ServerConfigData.serverBlockList,
            ServerConfigData.serverAllowList,
            ServerConfigData.serverBlockListMode,
        )
        for (player in server.playerList.players) {
            if (NetworkCompat.canSendConfig(player)) {
                NetworkCompat.sendConfig(player, payload)
            }
        }
    }
}
