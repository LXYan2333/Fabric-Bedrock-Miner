package com.github.lxyan2333.bedrockminer

import com.github.lxyan2333.bedrockminer.command.ServerCommands
import com.github.lxyan2333.bedrockminer.compat.NetworkCompat
import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.config.ServerConfigManager
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
//? if >=1.17 {
import org.slf4j.LoggerFactory
//?} else
//import org.apache.logging.log4j.LogManager

object BedrockMinerServer : DedicatedServerModInitializer {
    //? if >=1.17 {
    private val logger = LoggerFactory.getLogger("bedrock-miner")
    //?} else
    //private val logger = LogManager.getLogger("bedrock-miner")

    override fun onInitializeServer() {
        logger.info("Bedrock Miner initialized")
        ServerConfigManager.load()
        ModNetwork.registerPayloadTypes()
        ServerCommands.register()

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            ServerConfigManager.init(server)
        }

        ServerPlayConnectionEvents.JOIN.register { listener, _, _ ->
            val player = listener.player
            if (NetworkCompat.canSendConfig(player)) {
                val payload = ModNetwork.ConfigSyncPayload(
                    ServerConfigData.PROTOCOL_VERSION,
                    ServerConfigData.serverBlockList,
                    ServerConfigData.serverAllowList,
                    ServerConfigData.serverBlockListMode,
                )
                NetworkCompat.sendConfig(player, payload)
            }
        }
    }
}
