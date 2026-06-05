package com.github.lxyan2333.bedrockminer.client.event

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.Blocks
import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController

object ClientEventHandlers {

    fun register() {
        // Right-click bedrock with empty hand to toggle the mod on/off
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            if (!world.isClientSide) return@register InteractionResult.PASS
            val state = world.getBlockState(hitResult.blockPos)
            if (state.`is`(Blocks.BEDROCK) && player.mainHandItem.isEmpty) {
                BreakingFlowController.toggle()
                return@register InteractionResult.FAIL
            }
            InteractionResult.PASS
        }

        // Left-click bedrock to enqueue for breaking (click mode)
        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            if (!world.isClientSide) return@register InteractionResult.PASS
            val state = world.getBlockState(pos)
            if (state.`is`(Blocks.BEDROCK) && BreakingFlowController.isEnabled()) {
                BreakingFlowController.enqueueBlock(pos)
            }
            InteractionResult.PASS
        }
    }
}