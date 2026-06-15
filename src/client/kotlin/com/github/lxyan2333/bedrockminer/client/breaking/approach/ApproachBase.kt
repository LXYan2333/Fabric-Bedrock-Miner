package com.github.lxyan2333.bedrockminer.client.breaking.approach

import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController.isPositionProtected
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseTorchBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RedstoneWallTorchBlock

import com.github.lxyan2333.bedrockminer.client.config.ApproachMode
import com.github.lxyan2333.bedrockminer.client.config.Configs
import net.minecraft.world.level.block.piston.PistonBaseBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

abstract class ApproachBase internal constructor(
    val targetPos: BlockPos,
    val pistonPos: BlockPos,
    val extendDir: Direction,
    val torchPos: BlockPos,
    val supportBlockPos: BlockPos? = null,
) {
    val pushDir: Direction
        get() {
            for (face in Direction.entries) {
                if (pistonPos.relative(face) == targetPos) return face
            }
            error("pistonPos $pistonPos is not adjacent to targetPos $targetPos")
        }

    val extendPos: BlockPos get() = pistonPos.relative(extendDir)

    fun occupies(pos: BlockPos): Boolean =
        pos == pistonPos || pos == torchPos || pos == supportBlockPos || pos == extendPos

    // -- placement method --

    suspend fun placePiston(direction: Direction) {
        placePistonAfter(direction, {})
    }

    abstract suspend fun placePistonAfter(direction: Direction, pre: () -> Unit)

    // -- validation --

    fun quality(level: Level): Int? {
        val usedPos = listOf(pistonPos, torchPos, extendPos)
        if (usedPos.any { isPositionProtected(it) }) return null

        val player = Minecraft.getInstance().player ?: return null
        if (!player.isWithinBlockInteractionRange(pistonPos, 0.0)) return null
        if (!player.isWithinBlockInteractionRange(torchPos, 0.0)) return null

        if (!canPlaceBlock(level, player, pistonPos, Blocks.PISTON)) return null

        if (!level.getBlockState(extendPos).canBeReplaced()) return null


        if (!level.getBlockState(torchPos).canBeReplaced()) return null

        if (supportBlockPos == null && !torchCanSurvive(level, torchPos)) return null

        if (supportBlockPos != null) {
            if (isPositionProtected(supportBlockPos)) return null
            if (!player.isWithinBlockInteractionRange(supportBlockPos, 0.0)) return null
            if (!canPlaceBlock(level, player, supportBlockPos, supportBlock)) return null
        }

        if ((Blocks.PISTON as PistonBaseBlock).getNeighborSignal(level, pistonPos, pushDir)) return 2
        if (player.boundingBox.intersects(extendPos)) return 1
        return 0
    }

    protected fun canPlaceBlock(
        level: Level, player: Player, pos: BlockPos, block: Block,
    ): Boolean {
        val ctx = BlockPlaceContext(
            player, InteractionHand.MAIN_HAND, ItemStack(block),
            BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false),
        )
        return (block.asItem() as BlockItem).canPlace(ctx, block.defaultBlockState())
    }

    protected fun torchCanSurvive(level: Level, pos: BlockPos): Boolean {
        val standing = Blocks.REDSTONE_TORCH.defaultBlockState()
        if ((Blocks.REDSTONE_TORCH as BaseTorchBlock).canSurvive(standing, level, pos)) return true
        for (facing in Direction.Plane.HORIZONTAL) {
            val wall = Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, facing)
            if ((Blocks.REDSTONE_WALL_TORCH as RedstoneWallTorchBlock).canSurvive(wall, level, pos)) return true
        }
        return false
    }

    companion object {
        val supportBlock: Block
            get() = Configs.Generic.SUPPORT_BLOCK.blockStateValue.block

        fun findBest(level: Level, targetPos: BlockPos): ApproachBase? {
            return when (ApproachMode.valueOf(Configs.Generic.APPROACH_MODE.stringValue)) {
                ApproachMode.VANILLA_FAST -> VanillaFastApproach.findBest(level, targetPos)
                ApproachMode.CARPET_ACCURATE -> CarpetApproach.findBest(level, targetPos)
                ApproachMode.VANILLA_ALL_DIRECTION -> VanillaAllDirectionApproach.findBest(level, targetPos)
            }
        }

        internal fun <T : ApproachBase> findBest(
            level: Level, targetPos: BlockPos,
            allowedFaces: List<Direction>, allowedExtendDirs: List<Direction>,
            factory: (BlockPos, BlockPos, Direction, BlockPos, BlockPos?) -> T,
        ): T? {
            val player = Minecraft.getInstance().player ?: return null
            val playerEyePos = player.eyePosition
            val faces = allowedFaces.sortedBy { playerEyePos.distanceTo(targetPos.relative(it).center) }.take(5)
                .let { dirs -> if (dirs.size > 2) dirs.drop(1) + dirs.first() else dirs }

            val candidate = arrayOfNulls<ApproachBase>(3)

            for (face in faces) {
                val pistonPos = targetPos.relative(face)
                if (!level.getBlockState(pistonPos).canBeReplaced()) continue

                for (extendDir in allowedExtendDirs.sortedByDescending { playerEyePos.distanceTo(pistonPos.relative(it).center) }) {
                    val extendPos = pistonPos.relative(extendDir)
                    if (!level.getBlockState(extendPos).canBeReplaced()) continue

                    val torchDirs =
                        Direction.Plane.HORIZONTAL.sortedBy { playerEyePos.distanceTo(pistonPos.relative(it).center) }
                            .let { dirs -> dirs.drop(1) + dirs.first() }
                    for (torchDir in torchDirs) {
                        val torchPos = pistonPos.relative(torchDir)
                        if (torchPos == extendPos) continue
                        if (!level.getBlockState(torchPos).canBeReplaced()) continue

                        val a = factory(targetPos, pistonPos, extendDir, torchPos, null)
                        val qualityA = a.quality(level)
                        when (qualityA) {
                            0 -> return a
                            null -> {}
                            else -> if (candidate[qualityA] == null) candidate[qualityA] = a
                        }

                        val slimePos = torchPos.relative(Direction.DOWN)
                        val b = factory(targetPos, pistonPos, extendDir, torchPos, slimePos)
                        val qualityB = b.quality(level)
                        when (qualityB) {
                            0 -> return b
                            null -> {}
                            else -> if (candidate[qualityB] == null) candidate[qualityB] = b
                        }
                    }
                }
            }

            if (candidate[1] != null) return candidate[1] as T
            if (candidate[2] != null) return candidate[2] as T

            return null
        }
    }
}