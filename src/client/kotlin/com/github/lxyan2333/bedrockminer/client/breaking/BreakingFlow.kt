package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.breaking.approach.ApproachBase
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import com.github.lxyan2333.bedrockminer.client.message.Messager
import net.minecraft.world.level.block.piston.PistonBaseBlock

class BreakingFlow(val targetPos: BlockPos) {
    var currentApproach: ApproachBase? = null
        internal set

    suspend fun execute() {
        val level = Minecraft.getInstance().level ?: return
        if (!level.getBlockState(targetPos).`is`(Blocks.BEDROCK)) return


        repeat(MAX_RETRIES) {

            val missing = InventoryManager.checkRequiredItems()
            if (missing != null) {
                Messager.actionBar(missing)
                return
            }

            val approach = ApproachBase.findBest(level, targetPos) ?: run {
                Messager.actionBar("Cannot find a valid approach!")
                return
            }
            currentApproach = approach
            try {
                // Step 1: place piston and torch
                if (approach.slimePos != null) {
                    BlockPlacer.simpleBlockPlacement(approach.slimePos, Blocks.SLIME_BLOCK.asItem())
                }
                BlockPlacer.simpleBlockPlacement(approach.torchPos, Blocks.REDSTONE_TORCH.asItem())
                approach.placePiston(approach.extendDir)


                // Step 2: wait for piston to fully extend
                if (!waitFor(40) {
                        level.getBlockState(approach.extendPos).`is`(Blocks.PISTON_HEAD)
                                &&
                                level.getBlockState(approach.pistonPos).getValue(PistonBaseBlock.EXTENDED)
                    }) {
//                cleanup(level, approach)
//                return@repeat
                }

                // Step 3: one-tick — break torch, break piston, place piston facing bedrock
//            TickScheduler.awaitTicks(2)
                approach.placePistonAfter(approach.pushDir) {
                    BlockBreaker.breakBlock(approach.torchPos)
                    BlockBreaker.breakBlock(approach.pistonPos)
                }

                // Step 4: wait for piston to fully retract
//                waitFor(40) {
//                    !level.getBlockState(approach.bedrockPos).`is`(Blocks.BEDROCK)
//                }
                waitFor(40) {
                    !level.getBlockState(approach.pistonPos).`is`(Blocks.MOVING_PISTON)
                }
                waitFor(40) {
                    try {
                        (!level.getBlockState(approach.pistonPos)
                            .getValue(PistonBaseBlock.EXTENDED)) && (level.getBlockState(approach.pistonPos)
                            .getValue(PistonBaseBlock.FACING) == approach.extendDir)
                    } catch (_: IllegalArgumentException) {
                        return@waitFor true
                    }
                }
                TickScheduler.awaitTicks(1)

                // Step 5: verify
                cleanup(level, approach)
                if (!level.getBlockState(targetPos).`is`(Blocks.BEDROCK)) {
                    Messager.actionBar("Bedrock broken!")
                    return
                }
            } finally {
                currentApproach = null
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

    private suspend fun cleanup(level: Level, approach: ApproachBase) {
        BlockBreaker.breakBlock(approach.pistonPos)

        BlockBreaker.breakBlock(approach.torchPos)

        approach.slimePos?.let {
            BlockBreaker.breakBlock(it)
        }

        waitFor(40) {
            val ok = level.getBlockState(approach.pistonPos).canBeReplaced() && level.getBlockState(approach.torchPos)
                .canBeReplaced()

            if (approach.slimePos == null) ok else level.getBlockState(approach.slimePos).canBeReplaced() && ok
        }
    }

    companion object {
        private const val MAX_RETRIES = 5
    }
}