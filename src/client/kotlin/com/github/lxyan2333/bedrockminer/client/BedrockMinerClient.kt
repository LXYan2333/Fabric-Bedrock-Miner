package com.github.lxyan2333.bedrockminer.client

import com.github.lxyan2333.bedrockminer.client.config.Configs
import fi.dy.masa.malilib.config.ConfigManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.breaking.TickScheduler
import com.github.lxyan2333.bedrockminer.client.event.ClientEventHandlers
import net.minecraft.client.Minecraft

object BedrockMinerClient : ClientModInitializer {
    override fun onInitializeClient() {
        ConfigManager.getInstance().registerConfigHandler("bedrock-miner", Configs)
        ClientEventHandlers.register()

        Configs.init()
    }
}