package com.github.lxyan2333.bedrockminer

import net.fabricmc.api.DedicatedServerModInitializer
import org.slf4j.LoggerFactory

object BedrockMinerServer : DedicatedServerModInitializer {
    private val logger = LoggerFactory.getLogger("bedrock-miner")

    override fun onInitializeServer() {
        logger.info("Bedrock Miner initialized")
    }
}