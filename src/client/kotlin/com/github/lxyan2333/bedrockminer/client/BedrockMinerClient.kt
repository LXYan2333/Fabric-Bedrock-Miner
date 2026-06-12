package com.github.lxyan2333.bedrockminer.client

import com.github.lxyan2333.bedrockminer.client.config.Configs
import fi.dy.masa.malilib.config.ConfigManager
import net.fabricmc.api.ClientModInitializer
import com.github.lxyan2333.bedrockminer.client.event.ClientEventHandlers

object BedrockMinerClient : ClientModInitializer {
    override fun onInitializeClient() {
        ConfigManager.getInstance().registerConfigHandler("bedrock-miner", Configs)
        ClientEventHandlers.register()

        Configs.init()
    }
}