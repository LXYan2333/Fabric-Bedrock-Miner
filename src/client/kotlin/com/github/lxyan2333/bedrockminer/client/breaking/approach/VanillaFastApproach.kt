package com.github.lxyan2333.bedrockminer.client.breaking.approach

import com.github.lxyan2333.bedrockminer.client.breaking.BlockPlacer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

class VanillaFastApproach internal constructor(
    bedrockPos: BlockPos,
    pistonPos: BlockPos,
    extendDir: Direction,
    torchPos: BlockPos,
    slimePos: BlockPos? = null,
) : ApproachBase(bedrockPos, pistonPos, extendDir, torchPos, slimePos) {

    override suspend fun placePistonAfter(direction: Direction, pre: () -> Unit) {
        pre()
        BlockPlacer.vanillaPistonPlacement(pistonPos, direction)
    }

    companion object {
        private val FACES = listOf(Direction.UP, Direction.DOWN)
        private val EXTEND_DIRS = listOf(Direction.UP, Direction.DOWN)

        fun findBest(level: Level, bedrockPos: BlockPos): VanillaFastApproach? {
            return findBest(level, bedrockPos, FACES, EXTEND_DIRS, ::VanillaFastApproach)
        }
    }
}