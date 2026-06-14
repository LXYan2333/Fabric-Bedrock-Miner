package com.github.lxyan2333.bedrockminer

import com.github.lxyan2333.bedrockminer.config.ServerConfig
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.slf4j.LoggerFactory

object BedrockMinerServer : DedicatedServerModInitializer {
    private val logger = LoggerFactory.getLogger("bedrock-miner")

    override fun onInitializeServer() {
        logger.info("Bedrock Miner initialized")
        ServerConfig.load()
        ModNetwork.registerPayloadTypes()

        ServerPlayConnectionEvents.JOIN.register { listener, _, _ ->
            val player = listener.player
            val payload = ModNetwork.ConfigSyncPayload(
                ServerConfig.PROTOCOL_VERSION,
                ServerConfig.serverBlockList,
                ServerConfig.serverAllowList,
                ServerConfig.serverBlockListMode,
            )
            ServerPlayNetworking.send(player, payload)
        }
    }
}
