package com.github.lxyan2333.bedrockminer

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object BedrockMiner : ModInitializer {
    private val logger = LoggerFactory.getLogger("bedrock-miner")

    override fun onInitialize() {
        logger.info("Bedrock Miner initialized")
    }
}
