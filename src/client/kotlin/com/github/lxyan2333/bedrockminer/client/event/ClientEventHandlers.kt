package com.github.lxyan2333.bedrockminer.client.event

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.Blocks

import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.breaking.TickScheduler
import com.github.lxyan2333.bedrockminer.client.config.Configs
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft

object ClientEventHandlers {

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
            if (!BreakingFlowController.isInternalBreak &&
                BreakingFlowController.isPositionProtected(pos)
            ) {
                return@register InteractionResult.FAIL
            }
            if (world.getBlockState(pos).`is`(Blocks.BEDROCK) && BreakingFlowController.enabled) {
                BreakingFlowController.tryEnqueueBlock(pos)
                return@register InteractionResult.FAIL
            }
            return@register InteractionResult.PASS
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            if (Minecraft.getInstance().screen != null) {
                return@register
            }
            TickScheduler.onTick()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (Configs.BEDROCK_MINER_ENABLED.booleanValue) {
                BreakingFlowController.enable()
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            BreakingFlowController.onDisconnect()
        }
//        ClientPreAttackCallback.EVENT.register { minecraft, player, i ->
//            val camera = minecraft.cameraEntity ?: return@register false
//            val hitResult = player.raycastHitResult(1.0F, camera)
//            if (hitResult.type != HitResult.Type.BLOCK) return@register false
//            val blockHitResult = hitResult as BlockHitResult
//            val hitPos = blockHitResult.blockPos
//            val hitState = minecraft.level?.getBlockState(hitPos) ?: return@register false
//            if (hitState.`is`(Blocks.BEDROCK) && BreakingFlowController.enabled) {
//                BreakingFlowController.enqueueBlock(hitPos)
//                return@register true
//            }
//            return@register false
//        }
    }
}