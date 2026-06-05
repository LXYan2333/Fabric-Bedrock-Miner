package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseTorchBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RedstoneWallTorchBlock
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.piston.PistonBaseBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

data class Approach(
    val bedrockPos: BlockPos,
    val pistonPos: BlockPos,
    val extendDir: Direction,
    val torchPos: BlockPos,
    val slimePos: BlockPos? = null,
) {
    /** Direction piston pushes toward bedrock (step 3). */
    val pushDir: Direction get() {
        for (face in Direction.entries) {
            if (pistonPos.relative(face) == bedrockPos) return face
        }
        error("pistonPos $pistonPos is not adjacent to bedrockPos $bedrockPos")
    }

    val extendPos: BlockPos get(){
        return pistonPos.relative(extendDir)
    }

    /**
     * Validates this approach in the current world.
     * [rejectPlayerOverlap] = true → reject when piston head extends into player.
     */
    fun isValid(level: Level, rejectPlayerOverlap: Boolean): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        if (!player.isWithinBlockInteractionRange(pistonPos, 0.0)) return false
        if (!player.isWithinBlockInteractionRange(torchPos, 0.0)) return false

        if (!canPlaceBlock(level, player, pistonPos, Blocks.PISTON)) return false

        val extendPos = pistonPos.relative(extendDir)
        if (!PistonBaseBlock.isPushable(
                level.getBlockState(extendPos), level, extendPos, extendDir, true, extendDir
            )) return false

        if (rejectPlayerOverlap && player.boundingBox.intersects(extendPos)) return false

        if (!level.getBlockState(torchPos).canBeReplaced()) return false

        if (slimePos == null && !torchCanSurvive(level, torchPos)) return false

        if (slimePos != null) {
            if (!player.isWithinBlockInteractionRange(slimePos, 0.0)) return false
            if (!canPlaceBlock(level, player, slimePos, Blocks.SLIME_BLOCK)) return false
        }

        return true
    }

    private fun canPlaceBlock(level: Level, player: net.minecraft.world.entity.player.Player, pos: BlockPos, block: net.minecraft.world.level.block.Block): Boolean {
        val ctx = BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack(block), BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false))
        return (block.asItem() as BlockItem).canPlace(ctx, block.defaultBlockState())
    }

    private fun torchCanSurvive(level: Level, pos: BlockPos): Boolean {
        val standing = Blocks.REDSTONE_TORCH.defaultBlockState()
        if ((Blocks.REDSTONE_TORCH as BaseTorchBlock).canSurvive(standing, level, pos)) return true
        for (facing in Direction.Plane.HORIZONTAL) {
            val wall = Blocks.REDSTONE_WALL_TORCH.defaultBlockState()
                .setValue(RedstoneWallTorchBlock.FACING, facing)
            if ((Blocks.REDSTONE_WALL_TORCH as RedstoneWallTorchBlock).canSurvive(wall, level, pos)) return true
        }
        return false
    }

    companion object {
        fun findBest(level: Level, bedrockPos: BlockPos): Approach? {
            val player = Minecraft.getInstance().player?:return null
            val playerPos = player.position()
            val faces = Direction.entries.sortedBy { playerPos.distanceTo(bedrockPos.relative(it).center) }.slice(0..2)

            for (rejectOverlap in booleanArrayOf(true, false)) {
                for (face in faces) {
                    val pistonPos = bedrockPos.relative(face)
                    if (!level.getBlockState(pistonPos).canBeReplaced()) continue

                        val extendDir = player.nearestViewDirection.opposite
                        val extendPos = pistonPos.relative(extendDir)
                        if (!PistonBaseBlock.isPushable(level.getBlockState(extendPos), level, extendPos, extendDir, true, extendDir)) continue


                        for (torchDir in Direction.Plane.HORIZONTAL) {
                            val torchPos = pistonPos.relative(torchDir)
                            if (torchPos == extendPos) continue
                            if (!level.getBlockState(torchPos).canBeReplaced()) continue

                            val a = Approach(bedrockPos, pistonPos, extendDir, torchPos, null)
                            if (a.isValid(level, rejectOverlap)) return a

                            val b = Approach(bedrockPos, pistonPos, extendDir, torchPos, torchPos.relative(Direction.DOWN))
                            if (b.isValid(level, rejectOverlap)) return b

                        }

                }
            }
            return null
        }
    }
}