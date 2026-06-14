package com.github.lxyan2333.bedrockminer.client.network

import com.github.lxyan2333.bedrockminer.client.config.ClientConfigHandler
import com.github.lxyan2333.bedrockminer.client.message.Messager
import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.config.ServerConfigData.SPECIAL_BLOCKS
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object ClientNetworkHandler {
    fun registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.ConfigSyncPayload.TYPE) { payload, _ ->
            if (payload.protocolVersion != ServerConfigData.PROTOCOL_VERSION) {
                Messager.chat("§c[Bedrock Miner]§r Protocol version mismatch! Server: ${payload.protocolVersion}, Client: ${ServerConfigData.PROTOCOL_VERSION}. Using default config.")
                ClientConfigHandler.applyFromPacket(payload.protocolVersion, SPECIAL_BLOCKS, setOf(), "ALLOWED")
            } else {
                ClientConfigHandler.applyFromPacket(payload.protocolVersion, payload.blockList, payload.allowList, payload.blockListMode)
                Messager.chat("§a[Bedrock Miner]§r Server has Bedrock Miner installed!")
                Messager.chat("§7Block list:§r ${ServerConfigData.serverBlockList.ifEmpty { "(empty)" }}")
                Messager.chat("§7Allow list:§r ${ServerConfigData.serverAllowList.ifEmpty { "(empty)" }}")
                Messager.chat("§7Block list mode:§r ${ServerConfigData.serverBlockListMode}")
            }
        }
    }
}
