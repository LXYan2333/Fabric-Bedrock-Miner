package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.breaking.approach.ApproachBase
import com.github.lxyan2333.bedrockminer.client.compat.MinecraftClientCompat
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import com.github.lxyan2333.bedrockminer.client.config.Configs
import com.github.lxyan2333.bedrockminer.client.message.Messager
import net.minecraft.world.level.block.piston.PistonBaseBlock
import net.minecraft.world.level.block.state.BlockState
import fi.dy.masa.malilib.util.StringUtils

class BreakingFlow(val targetPos: BlockPos, val targetBlockState: BlockState) {
    var currentApproach: ApproachBase? = null
        internal set

    var doCleanUp: Boolean = true

    private val targetBlockName: String
        get() = targetBlockState.block.name.string

    private val supportBlock: Block
        get() = Configs.Generic.supportBlock

    suspend fun execute() {
        val level = Minecraft.getInstance().level ?: return
        if (level.getBlockState(targetPos) != targetBlockState) return


        repeat(Configs.Generic.MAX_RETRIES.integerValue) {

            val missing = InventoryManager.checkRequiredItems()
            if (missing != null) {
                Messager.actionBar(missing)
                return
            }

            val approach = ApproachBase.findBest(level, targetPos) ?: run {
                Messager.actionBar(
                    StringUtils.translate(
                        "bedrockminer.message.cannot_find_approach",
                        Configs.Generic.APPROACH_MODE.optionListValue.displayName
                    )
                )
                return
            }
            currentApproach = approach
            try {
                // Step 1: place piston and torch
                if (approach.supportBlockPos != null) {
                    BlockPlacer.simpleBlockPlacement(approach.supportBlockPos, supportBlock.asItem())
                }
                BlockPlacer.simpleBlockPlacement(approach.torchPos, Blocks.REDSTONE_TORCH.asItem())
                approach.placePiston(approach.extendDir)

                // Step 2: wait for piston to fully extend
                waitFor(Configs.Generic.WAIT_TICKS.integerValue) {
                    try {
                        level.getBlockState(approach.extendPos)
                            .`is`(Blocks.PISTON_HEAD) && level.getBlockState(approach.pistonPos)
                            .getValue(PistonBaseBlock.EXTENDED)
                    } catch (_: IllegalArgumentException) {
                        false
                    }
                }

                // Step 3: one-tick — break torch, break piston, place piston facing target
                approach.placePistonAfter(approach.pushDir) {
                    BlockBreaker.breakBlock(approach.torchPos)
                    BlockBreaker.breakBlock(approach.pistonPos)
                }

                // Step 4: wait for piston to fully retract
                waitFor(Configs.Generic.WAIT_TICKS.integerValue) {
                    !level.getBlockState(approach.pistonPos).`is`(Blocks.MOVING_PISTON)
                }
                waitFor(Configs.Generic.WAIT_TICKS.integerValue) {
                    try {
                        (!level.getBlockState(approach.pistonPos)
                            .getValue(PistonBaseBlock.EXTENDED)) && (level.getBlockState(approach.pistonPos)
                            .getValue(PistonBaseBlock.FACING) == approach.extendDir)
                    } catch (_: IllegalArgumentException) {
                        return@waitFor false
                    }
                }

                // Step 5: verify
                if (level.getBlockState(targetPos) != targetBlockState) {
                    Messager.actionBar(StringUtils.translate("bedrockminer.message.block_broken", targetBlockName))
                    return
                }
            } catch (_: BlockInteractionRangeException) {
                Messager.actionBar(StringUtils.translate("bedrockminer.message.out_of_range"))
            } finally {
                currentApproach = null
                if (doCleanUp) {
                    cleanup(level, approach)
                }
            }
        }

        Messager.actionBar(StringUtils.translate("bedrockminer.message.breaking_failed", targetBlockName))
    }

    private suspend fun waitFor(maxTicks: Int, condition: () -> Boolean): Boolean {
        repeat(maxTicks) {
            if (condition()) return true
            ClientTickScheduler.awaitTicks(1)
        }
        return condition()
    }

    private suspend fun cleanup(level: Level, approach: ApproachBase) {
        try {
            BlockBreaker.breakBlock(approach.pistonPos)

            BlockBreaker.breakBlock(approach.torchPos)

            approach.supportBlockPos?.let {
                BlockBreaker.breakBlock(it)
            }
        } catch (_: BlockInteractionRangeException) {
        }

        waitFor(Configs.Generic.WAIT_TICKS.integerValue) {
            val ok = MinecraftClientCompat.canBeReplaced(level, approach.pistonPos) &&
                MinecraftClientCompat.canBeReplaced(level, approach.torchPos)

            ok && approach.supportBlockPos?.let { MinecraftClientCompat.canBeReplaced(level, it) } ?: true
        }
    }
}
