package com.github.lxyan2333.bedrockminer.client.breaking.approach

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
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

open abstract class ApproachBase internal constructor(
    val bedrockPos: BlockPos,
    val pistonPos: BlockPos,
    val extendDir: Direction,
    val torchPos: BlockPos,
    val slimePos: BlockPos? = null,
) {
    val pushDir: Direction
        get() {
            for (face in Direction.entries) {
                if (pistonPos.relative(face) == bedrockPos) return face
            }
            error("pistonPos $pistonPos is not adjacent to bedrockPos $bedrockPos")
        }

    val extendPos: BlockPos get() = pistonPos.relative(extendDir)

    fun occupies(pos: BlockPos): Boolean =
        pos == pistonPos || pos == torchPos || pos == slimePos || pos == extendPos

    // -- placement method --

    open suspend fun prePlacePiston(direction: Direction) {}

    abstract fun placePiston(direction: Direction)

    // -- validation --

    fun isValid(level: Level, rejectPlayerOverlap: Boolean): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        if (!player.isWithinBlockInteractionRange(pistonPos, 0.0)) return false
        if (!player.isWithinBlockInteractionRange(torchPos, 0.0)) return false

        if (!canPlaceBlock(level, player, pistonPos, Blocks.PISTON)) return false

        if (!level.getBlockState(extendPos).canBeReplaced()) return false

        if (rejectPlayerOverlap && player.boundingBox.intersects(extendPos)) return false

        if (!level.getBlockState(torchPos).canBeReplaced()) return false

        if (slimePos == null && !torchCanSurvive(level, torchPos)) return false

        if (slimePos != null) {
            if (!player.isWithinBlockInteractionRange(slimePos, 0.0)) return false
            if (!canPlaceBlock(level, player, slimePos, Blocks.SLIME_BLOCK)) return false
        }

        return true
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
        fun findBest(level: Level, bedrockPos: BlockPos): ApproachBase? {
            return when (ApproachMode.valueOf(Configs.APPROACH_MODE.stringValue)) {
                ApproachMode.VANILLA_FAST -> VanillaFastApproach.findBest(level, bedrockPos)
                ApproachMode.CARPET_ACCURATE -> CarpetApproach.findBest(level, bedrockPos)
                ApproachMode.VANILLA_ALL_DIRECTION -> VanillaAllDirectionApproach.findBest(level, bedrockPos)
            }
        }

        internal fun <T : ApproachBase> findBest(
            level: Level, bedrockPos: BlockPos,
            allowedFaces: List<Direction>, allowedExtendDirs: List<Direction>,
            factory: (BlockPos, BlockPos, Direction, BlockPos, BlockPos?) -> T,
        ): T? {
            val player = Minecraft.getInstance().player ?: return null
            val playerPos = player.position()
            val faces = allowedFaces.sortedBy { playerPos.distanceTo(bedrockPos.relative(it).center) }.take(3)

            for (rejectOverlap in booleanArrayOf(true, false)) {
                for (face in faces) {
                    val pistonPos = bedrockPos.relative(face)
                    if (!level.getBlockState(pistonPos).canBeReplaced()) continue

                    for (extendDir in allowedExtendDirs) {
                        val extendPos = pistonPos.relative(extendDir)
                        if (!level.getBlockState(extendPos).canBeReplaced()) continue

                        for (torchDir in Direction.Plane.HORIZONTAL) {
                            val torchPos = pistonPos.relative(torchDir)
                            if (torchPos == extendPos) continue
                            if (!level.getBlockState(torchPos).canBeReplaced()) continue

                            val a = factory(bedrockPos, pistonPos, extendDir, torchPos, null)
                            if (a.isValid(level, rejectOverlap)) return a

                            val slimePos = torchPos.relative(Direction.DOWN)
                            val b = factory(bedrockPos, pistonPos, extendDir, torchPos, slimePos)
                            if (b.isValid(level, rejectOverlap)) return b
                        }
                    }
                }
            }
            return null
        }
    }
}