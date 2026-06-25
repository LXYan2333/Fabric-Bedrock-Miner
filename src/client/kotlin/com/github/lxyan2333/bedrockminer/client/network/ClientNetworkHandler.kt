package com.github.lxyan2333.bedrockminer.client.network

import com.github.lxyan2333.bedrockminer.client.config.ClientConfigHandler
import com.github.lxyan2333.bedrockminer.client.message.Messager
import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.config.ServerConfigData.SPECIAL_BLOCKS
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import fi.dy.masa.malilib.util.StringUtils
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object ClientNetworkHandler {
    fun registerClientHandlers() {
        //? if >=1.20.5 {
        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.ConfigSyncPayload.TYPE) { payload, _ ->
            handleConfigSync(payload)
        }
        //?} else if >=1.20 {
        /*ClientPlayNetworking.registerGlobalReceiver(ModNetwork.ConfigSyncPayload.TYPE) { payload, _, _ ->
            handleConfigSync(payload)
        }
        *///?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(ModNetwork.CONFIG_SYNC_ID) { _, _, buffer, _ ->
            handleConfigSync(ModNetwork.ConfigSyncPayload.readFromBuffer(buffer))
        }
        *///?}
    }

    private fun handleConfigSync(payload: ModNetwork.ConfigSyncPayload) {
        if (payload.protocolVersion != ServerConfigData.PROTOCOL_VERSION) {
            Messager.chat(
                StringUtils.translate(
                    "bedrockminer.message.protocol_mismatch",
                    payload.protocolVersion.toString(),
                    ServerConfigData.PROTOCOL_VERSION.toString()
                )
            )
            ClientConfigHandler.applyFromPacket(payload.protocolVersion, SPECIAL_BLOCKS, setOf(), "ALLOWED")
        } else {
            ClientConfigHandler.applyFromPacket(
                payload.protocolVersion, payload.blockList, payload.allowList, payload.blockListMode
            )
            Messager.chat(StringUtils.translate("bedrockminer.message.server_installed"))
            Messager.chat(
                StringUtils.translate(
                    "bedrockminer.message.server_block_list", formatBlockList(ServerConfigData.serverBlockList)
                )
            )
            Messager.chat(
                StringUtils.translate(
                    "bedrockminer.message.server_allow_list", formatBlockList(ServerConfigData.serverAllowList)
                )
            )
            Messager.chat(
                StringUtils.translate(
                    "bedrockminer.message.server_block_list_mode", ServerConfigData.serverBlockListMode
                )
            )
        }
    }

    private fun formatBlockList(blocks: Set<String>): String {
        if (blocks.isEmpty()) return StringUtils.translate("bedrockminer.message.empty")
        return blocks.joinToString(", ") { blockId ->
            try {
                val block = IdentifierCompat.block(blockId)!!
                block.name.string
            } catch (_: Exception) {
                blockId
            }
        }
    }
}
