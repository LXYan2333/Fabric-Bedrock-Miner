package com.github.lxyan2333.bedrockminer.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.breaking.TickScheduler
import com.github.lxyan2333.bedrockminer.client.event.ClientEventHandlers

object BedrockMinerClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientEventHandlers.register()

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            TickScheduler.onTick()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            BreakingFlowController.enable()
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            BreakingFlowController.onDisconnect()
        }
    }
}