package com.github.lxyan2333.bedrockminer.client.breaking.approach

import com.github.lxyan2333.bedrockminer.client.breaking.BlockPlacer
import com.github.lxyan2333.bedrockminer.client.breaking.ClientTickScheduler
import com.github.lxyan2333.bedrockminer.client.config.Configs
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

    override suspend fun placePistonAfter(direction: Direction, pre: () -> Unit) {
        mutex.withLock {
            try {
                currentYawPitch = BlockPlacer.vanillaPistonPlacement1(direction)
                ClientTickScheduler.awaitTicks(Configs.Server.WAIT_SERVER_TICK_PLAYER_ENTITY_TICKS.integerValue)
                pre()
                BlockPlacer.vanillaPistonPlacement2(pistonPos, direction)
            } finally {
                currentYawPitch = null
            }
        }
    }

    companion object {
        private val mutex = Mutex()

        @JvmField
        var currentYawPitch: Pair<Float, Float>? = null

        fun findBest(level: Level, targetPos: BlockPos): VanillaAllDirectionApproach? {
            return findBest(level, targetPos, Direction.entries, Direction.entries, ::VanillaAllDirectionApproach)
        }
    }
}
