package com.github.lxyan2333.bedrockminer.client.event

import com.github.lxyan2333.bedrockminer.client.breaking.BlockBreaker
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.Blocks

import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.breaking.TickScheduler
import com.github.lxyan2333.bedrockminer.client.config.BlockListMode
import com.github.lxyan2333.bedrockminer.client.config.Configs
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.state.BlockState

object ClientEventHandlers {

    private fun isBlockAllowed(blockState: BlockState): Boolean {
        val blockId = BuiltInRegistries.BLOCK.getKey(blockState.block).toString()

        if (Configs.Client.ALLOW_LIST.strings.contains(blockId)) {
            return true
        }

        val mode = Configs.Client.BLOCK_LIST_MODE.optionListValue as BlockListMode
        val BlockListContains = Configs.Client.BLOCK_LIST.strings.contains(blockId)

        return when (mode) {
            BlockListMode.BLOCKLIST -> !BlockListContains
            BlockListMode.ALLOWLIST -> BlockListContains
        }
    }

    fun register() {
        // Right-click bedrock with empty hand to toggle the mod on/off
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            val state = world.getBlockState(hitResult.blockPos)
            if (state.`is`(Blocks.BEDROCK) && player.mainHandItem.isEmpty) {
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
            if (!BreakingFlowController.isInternalBreak &&
                BreakingFlowController.isPositionProtected(pos)
            ) {
                return@register InteractionResult.FAIL
            }
            val blockState = world.getBlockState(pos)
            if (!isBlockAllowed(blockState)) {
                return@register InteractionResult.PASS
            }
            BreakingFlowController.tryEnqueueBlock(pos)
            return@register InteractionResult.FAIL
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            if (Minecraft.getInstance().screen != null) {
                return@register
            }
            TickScheduler.onTick()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (Configs.Generic.BEDROCK_MINER_ENABLED.booleanValue) {
                BreakingFlowController.enable()
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            BreakingFlowController.onDisconnect()
        }
    }
}