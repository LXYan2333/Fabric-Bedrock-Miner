package com.github.lxyan2333.bedrockminer.client.breaking.approach

import com.github.lxyan2333.bedrockminer.client.breaking.BlockPlacer
import com.github.lxyan2333.bedrockminer.client.breaking.ClientTickScheduler
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

class VanillaAllDirectionApproach internal constructor(
    targetPos: BlockPos,
    pistonPos: BlockPos,
    extendDir: Direction,
    torchPos: BlockPos,
    slimePos: BlockPos? = null,
) : ApproachBase(targetPos, pistonPos, extendDir, torchPos, slimePos) {
    private val mutex = Mutex()

    override suspend fun placePistonAfter(direction: Direction, pre: () -> Unit) {
        mutex.withLock {
            BlockPlacer.vanillaPistonPlacement1(direction)
            ClientTickScheduler.awaitTicks(2)
            pre()
            BlockPlacer.vanillaPistonPlacement2(pistonPos, direction)
        }
    }

    companion object {
        fun findBest(level: Level, targetPos: BlockPos): VanillaAllDirectionApproach? {
            return findBest(level, targetPos, Direction.entries, Direction.entries, ::VanillaAllDirectionApproach)
        }
    }
}
