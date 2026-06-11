package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.Items

object BlockBreaker {
    fun breakBlock(pos: BlockPos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE)
        val gameMode = Minecraft.getInstance().gameMode ?: return
        BreakingFlowController.isInternalBreak = true
        try {
            gameMode.startDestroyBlock(pos, Direction.UP)
            gameMode.stopDestroyBlock()
        } finally {
            BreakingFlowController.isInternalBreak = false
        }
    }
}