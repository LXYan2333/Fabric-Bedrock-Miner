package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object CheckingEnvironment {

    fun findNearbyFlatBlockToPlaceRedstoneTorch(level: Level, blockPos: BlockPos): BlockPos? {
        for (direction in Direction.Plane.HORIZONTAL) {
            val base = blockPos.relative(direction)
            val above = base.above()
            if (Block.canSupportCenter(level, base, Direction.UP) &&
                (level.getBlockState(above).canBeReplaced() || level.getBlockState(above).`is`(Blocks.REDSTONE_TORCH))
            ) {
                return base
            }
        }
        return null
    }

    fun findPossibleSlimeBlockPos(level: Level, blockPos: BlockPos): BlockPos? {
        for (direction in Direction.Plane.HORIZONTAL) {
            val pos = blockPos.relative(direction)
            if (!level.getBlockState(pos).canBeReplaced()) continue
            if (isBlocked(pos)) continue
            return pos
        }
        return null
    }

    fun has2BlocksOfPlaceToPlacePiston(level: Level, blockPos: BlockPos): Boolean {
        val above = blockPos.above()
        if (level.getBlockState(above).getDestroySpeed(level, above) == 0f) {
            BlockBreaker.breakBlock(above)
        }
        if (isBlocked(above)) return false
        return level.getBlockState(above.above()).canBeReplaced()
    }

    fun findNearbyRedstoneTorch(level: Level, pistonPos: BlockPos, axis: Direction.Axis): List<BlockPos> {
        return Direction.entries
            .filter { it.axis != axis }
            .map { pistonPos.relative(it) }
            .filter { level.getBlockState(it).`is`(Blocks.REDSTONE_TORCH) }
    }

    private fun isBlocked(blockPos: BlockPos): Boolean {
        val client = Minecraft.getInstance()
        val player = client.player ?: return true
        val context = BlockPlaceContext(
            player,
            InteractionHand.MAIN_HAND,
            Blocks.SLIME_BLOCK.asItem().defaultInstance,
            BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false)
        )
        return !Blocks.SLIME_BLOCK.asItem().useOn(context).consumesAction()
    }
}