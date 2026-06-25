package com.github.lxyan2333.bedrockminer.client

import com.github.lxyan2333.bedrockminer.client.event.ClientEventHandlers
import net.fabricmc.api.ClientModInitializer

object BedrockMinerClient : ClientModInitializer {
    override fun onInitializeClient() {

        ClientEventHandlers.register()

    }
}
