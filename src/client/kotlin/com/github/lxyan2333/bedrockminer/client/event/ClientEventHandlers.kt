package com.github.lxyan2333.bedrockminer.client.event

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.world.InteractionResult
import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.breaking.ClientTickScheduler
import com.github.lxyan2333.bedrockminer.client.config.AllowOrBlockMode
import com.github.lxyan2333.bedrockminer.client.config.Configs
import com.github.lxyan2333.bedrockminer.client.config.ClientConfigHandler
import com.github.lxyan2333.bedrockminer.client.message.Messager
import com.github.lxyan2333.bedrockminer.client.network.ClientNetworkHandler
import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.state.BlockState

object ClientEventHandlers {
    private var lastLevel: ClientLevel? = null

    private fun isBlockAllowed(blockState: BlockState): Boolean {
        val blockId = BuiltInRegistries.BLOCK.getKey(blockState.block).toString()

        // Always block special blocks unless server explicitly allows them
        val isIntegratedServer = Minecraft.getInstance().singleplayerServer != null
        if (!ServerConfigData.serverHasMod && !isIntegratedServer && ServerConfigData.SPECIAL_BLOCKS.contains(blockId)) {
            return false
        }

        if (ServerConfigData.serverHasMod && !isIntegratedServer) {
            // Server block list takes precedence
            if (ServerConfigData.serverBlockList.contains(blockId)) {
                return false
            }

            // Server allow list
            if (!ServerConfigData.serverAllowList.contains(blockId)) {
                if (ServerConfigData.serverBlockListMode == "BLOCKED") {
                    return false
                }
            }
        }

        // Client allow list
        if (Configs.Client.ALLOW_LIST.strings.contains(blockId)) {
            return true
        }

        // Client block list
        if (Configs.Client.BLOCK_LIST.strings.contains(blockId)) {
            return false
        }

        // client mode
        val clientMode = Configs.Client.AlloeOrBlockMode.optionListValue as AllowOrBlockMode
        when (clientMode) {
            AllowOrBlockMode.BLOCKED -> return false
            AllowOrBlockMode.ALLOWED -> return true
        }
    }

    fun register() {
        ModNetwork.registerPayloadTypes()
        ClientNetworkHandler.registerClientHandlers()
        // Right-click block with empty hand to toggle the mod on/off
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            val state = world.getBlockState(hitResult.blockPos)
            if (isBlockAllowed(state) && player.mainHandItem.isEmpty) {
                BreakingFlowController.toggle()
                return@register InteractionResult.FAIL
            }
            InteractionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            if (!world.isClientSide) {
                return@register InteractionResult.PASS
            }
            if (!BreakingFlowController.enabled) {
                return@register InteractionResult.PASS
            }
            if (!BreakingFlowController.isInternalBreak && BreakingFlowController.isPositionProtected(pos)) {
                return@register InteractionResult.FAIL
            }
            val blockState = world.getBlockState(pos)
            if (!isBlockAllowed(blockState)) {
                return@register InteractionResult.PASS
            }
            BreakingFlowController.tryEnqueueBlock(pos)
            return@register InteractionResult.FAIL
        }

        ClientTickEvents.START_CLIENT_TICK.register { client ->
            val currentLevel = client.level ?: return@register
            if (currentLevel != lastLevel) {
                lastLevel = currentLevel
                BreakingFlowController.cancelAllFlows()
            }
            if (client.screen != null) {
                return@register
            }
            ClientTickScheduler.onTick()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            if (client.singleplayerServer == null) {
                if (ClientPlayNetworking.canSend(ModNetwork.DummyPayload.TYPE)) {
                    Messager.chat("server installed this mod")
                } else {
                    Messager.chat("server not installed this mod")
                }
            }
            if (Configs.Generic.BEDROCK_MINER_ENABLED.booleanValue) {
                BreakingFlowController.enable()
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            BreakingFlowController.onDisconnect()
            ClientConfigHandler.resetOnDisconnect()
        }
    }
}