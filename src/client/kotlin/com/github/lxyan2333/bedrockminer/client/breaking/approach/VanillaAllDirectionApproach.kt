package com.github.lxyan2333.bedrockminer.client.breaking.approach

import com.github.lxyan2333.bedrockminer.client.breaking.BlockPlacer
import kotlinx.coroutines.sync.Mutex
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

class VanillaAllDirectionApproach internal constructor(
    bedrockPos: BlockPos,
    pistonPos: BlockPos,
    extendDir: Direction,
    torchPos: BlockPos,
    slimePos: BlockPos? = null,
) : ApproachBase(bedrockPos, pistonPos, extendDir, torchPos, slimePos) {
    private val mutex = Mutex()

    override suspend fun prePlacePiston(direction: Direction) {
        mutex.lock()
        BlockPlacer.vanillaPistonPlacement1(direction)
    }

    override fun placePiston(direction: Direction) {
        BlockPlacer.vanillaPistonPlacement2(pistonPos, direction)
        mutex.unlock()
    }

    companion object {
        fun findBest(level: Level, bedrockPos: BlockPos): VanillaAllDirectionApproach? {
            return findBest(level, bedrockPos, Direction.entries, Direction.entries, ::VanillaAllDirectionApproach)
        }
    }
}