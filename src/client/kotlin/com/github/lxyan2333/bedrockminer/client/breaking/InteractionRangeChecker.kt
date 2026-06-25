package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.compat.MinecraftClientCompat
import net.minecraft.core.BlockPos

object InteractionRangeChecker {
    fun checkRange(pos: BlockPos) {
        if (!MinecraftClientCompat.canInteractWithBlock(pos)) {
            throw BlockInteractionRangeException(pos)
        }
    }
}
