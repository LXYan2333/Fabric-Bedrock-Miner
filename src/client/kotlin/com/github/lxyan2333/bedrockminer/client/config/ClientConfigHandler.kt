package com.github.lxyan2333.bedrockminer.client.config

import com.github.lxyan2333.bedrockminer.config.ServerConfigData

object ClientConfigHandler {
    fun applyFromPacket(protocolVersion: Int, blockList: Set<String>, allowList: Set<String>, blockListMode: String) {
        ServerConfigData.serverHasMod = true
        ServerConfigData.serverProtocolVersion = protocolVersion
        ServerConfigData.serverBlockList = blockList.toMutableSet()
        ServerConfigData.serverAllowList = allowList.toMutableSet()
        ServerConfigData.serverBlockListMode = blockListMode
    }

    fun resetOnDisconnect() {
        ServerConfigData.serverHasMod = false
        ServerConfigData.serverProtocolVersion = -1
        ServerConfigData.serverBlockList = mutableSetOf()
        ServerConfigData.serverAllowList = mutableSetOf()
        ServerConfigData.serverBlockListMode = "BLOCKED"
    }
}
