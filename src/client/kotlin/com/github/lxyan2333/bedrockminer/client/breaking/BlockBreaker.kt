package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.piston.PistonBaseBlock

object BlockBreaker {
    fun breakBlock(pos: BlockPos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE)
        Minecraft.getInstance().gameMode?.startDestroyBlock(pos, Direction.UP)
    }
}