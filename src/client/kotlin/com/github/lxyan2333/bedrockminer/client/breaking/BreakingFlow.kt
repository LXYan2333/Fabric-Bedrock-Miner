package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.piston.PistonBaseBlock
import com.github.lxyan2333.bedrockminer.client.message.Messager

class BreakingFlow(private val targetPos: BlockPos) {

    suspend fun execute() {
        val level = Minecraft.getInstance().level ?: return
        if (!level.getBlockState(targetPos).`is`(Blocks.BEDROCK)) return

        val missing = InventoryManager.checkRequiredItems()
        if (missing != null) {
            Messager.actionBar(missing)
            return
        }

        repeat(MAX_RETRIES) {
            val approach = Approach.findBest(level, targetPos) ?: run {
                Messager.actionBar("Cannot find a valid approach!")
                return
            }

            // Step 1: place piston and torch
            if (approach.slimePos != null) {
                BlockPlacer.simpleBlockPlacement(approach.slimePos, Blocks.SLIME_BLOCK.asItem())
            }
            BlockPlacer.simpleBlockPlacement(approach.torchPos, Blocks.REDSTONE_TORCH.asItem())
            TickScheduler.awaitTicks(2)
            BlockPlacer.pistonPlacement(approach.pistonPos, approach.extendDir)


            // Step 2: wait for piston to fully extend
            if (!waitFor(40) {
                    level.getBlockState(approach.extendPos).`is`(Blocks.PISTON_HEAD)
                }) {
                cleanup(level, approach)
                return@repeat
            }

            // Step 3: one-tick — break torch, break piston, place piston facing bedrock
//            TickScheduler.awaitTicks(2)
            BlockBreaker.breakBlock(approach.torchPos)
            BlockBreaker.breakBlock(approach.pistonPos)
            BlockPlacer.pistonPlacement(approach.pistonPos, approach.pushDir)


            // Step 4: wait for piston to fully retract
            waitFor(40) {
                !level.getBlockState(approach.bedrockPos).`is`(Blocks.BEDROCK)
            }
            waitFor(40) {
                level.getBlockState(approach.pistonPos).`is`(Blocks.PISTON)
            }
            TickScheduler.awaitTicks(2)

            // Step 5: verify
            cleanup(level, approach)
            if (!level.getBlockState(targetPos).`is`(Blocks.BEDROCK)) {
                Messager.actionBar("Bedrock broken!")
                return
            }
        }

        Messager.actionBar("Bedrock breaking failed!")
    }

    private suspend fun waitFor(maxTicks: Int, condition: () -> Boolean): Boolean {
        repeat(maxTicks) {
            if (condition()) return true
            TickScheduler.awaitTicks(1)
        }
        return condition()
    }

    private fun placeTorchIfNeeded(level: Level, torchPos: BlockPos) {
        if (!level.getBlockState(torchPos).`is`(Blocks.REDSTONE_TORCH)) {
            InventoryManager.switchToItem(Blocks.REDSTONE_TORCH.asItem())
            BlockPlacer.simpleBlockPlacement(torchPos, Blocks.REDSTONE_TORCH.asItem())
        }
    }

    private fun cleanup(level: Level, approach: Approach) {
        BlockBreaker.breakBlock(approach.pistonPos)
        approach.slimePos?.let {
            BlockBreaker.breakBlock(it)
        }
    }

    companion object {
        private const val MAX_RETRIES = 5
    }
}