package com.github.lxyan2333.bedrockminer.client.breaking.approach

import com.github.lxyan2333.bedrockminer.client.breaking.BlockPlacer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

/**
 * Carpet Extra approach: supports all piston facings via the accurate block
 * placement protocol. Direction is encoded in the hit X coordinate.
 * Requires [Carpet Extra](https://github.com/gnembon/fabric-carpet) on the server
 * with `accurateBlockPlacement` enabled.
 */
class CarpetApproach internal constructor(
    bedrockPos: BlockPos,
    pistonPos: BlockPos,
    extendDir: Direction,
    torchPos: BlockPos,
    slimePos: BlockPos? = null,
) : ApproachBase(bedrockPos, pistonPos, extendDir, torchPos, slimePos) {

    override suspend fun placePistonAfter(direction: Direction, pre: suspend () -> Unit) {
        pre()
        BlockPlacer.carpetPistonPlacement(pistonPos, direction)
    }

    companion object {
        fun findBest(level: Level, bedrockPos: BlockPos): CarpetApproach? {
            return findBest(level, bedrockPos, Direction.entries, Direction.entries, ::CarpetApproach)
        }
    }
}