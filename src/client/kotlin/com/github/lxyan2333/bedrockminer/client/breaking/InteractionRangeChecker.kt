package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos

object InteractionRangeChecker {
    fun checkRange(pos: BlockPos) {
        val player = Minecraft.getInstance().player ?: return
        if (!player.isWithinBlockInteractionRange(pos, 0.0)) {
            throw BlockInteractionRangeException(pos)
        }
    }
}
