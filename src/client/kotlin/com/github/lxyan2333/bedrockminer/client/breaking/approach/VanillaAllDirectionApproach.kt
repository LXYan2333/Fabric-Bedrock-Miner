package com.github.lxyan2333.bedrockminer.client.breaking.approach

import com.github.lxyan2333.bedrockminer.client.breaking.BlockPlacer
import com.github.lxyan2333.bedrockminer.client.breaking.TickScheduler
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    override suspend fun placePistonAfter(direction: Direction, pre: () -> Unit) {
        mutex.withLock {
            BlockPlacer.vanillaPistonPlacement1(direction)
            TickScheduler.awaitTicks(2)
            pre()
            BlockPlacer.vanillaPistonPlacement2(pistonPos, direction)
        }
    }

    companion object {
        fun findBest(level: Level, bedrockPos: BlockPos): VanillaAllDirectionApproach? {
            return findBest(level, bedrockPos, Direction.entries, Direction.entries, ::VanillaAllDirectionApproach)
        }
    }
}
