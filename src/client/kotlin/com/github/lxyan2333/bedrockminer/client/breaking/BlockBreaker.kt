package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.config.Configs
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.chunk.RenderRegionCache
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.Items

object BlockBreaker {
    private val pendingUpdates = mutableSetOf<BlockPos>()

    fun breakBlock(pos: BlockPos) {
        InteractionRangeChecker.checkRange(pos)
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE)
        val gameMode = Minecraft.getInstance().gameMode ?: return
        BreakingFlowController.isInternalBreak = true
        try {
            gameMode.startDestroyBlock(pos, Direction.UP)
        } finally {
            BreakingFlowController.isInternalBreak = false
            markForUpdate(pos)
        }
    }

    private fun markForUpdate(pos: BlockPos) {
        if (Configs.Generic.REMOVE_GHOST_BLOCKS.booleanValue) {
            pendingUpdates.add(pos)
        }
    }

    fun removeAllGhostBlock() {
        if (pendingUpdates.isEmpty()) return
        //? if <1.20.2 {
        /*val mc = Minecraft.getInstance()

        try {
            mc.execute {
                val renderer = mc.levelRenderer
                val viewArea = renderer.viewArea ?: return@execute
                val dispatcher = renderer.chunkRenderDispatcher ?: return@execute
                val cache = RenderRegionCache()

                for (pos in pendingUpdates) {
                    val section = viewArea.getRenderChunkAt(pos) ?: continue
                    dispatcher.rebuildChunkSync(section,cache)
                    section.setNotDirty()
                }
            }
        } finally {
            pendingUpdates.clear()
        }
        *///?} else {

        val mc = Minecraft.getInstance()

        try {
            mc.execute {
                val renderer = mc.levelRenderer
                val viewArea = renderer.viewArea ?: return@execute
                val dispatcher = renderer.sectionRenderDispatcher ?: return@execute
                val cache = RenderRegionCache()

                for (pos in pendingUpdates) {
                    val section = viewArea.getRenderSectionAt(pos) ?: continue
                    dispatcher.rebuildSectionSync(section, cache)
                    section.setNotDirty()
                }
            }
        } finally {
            pendingUpdates.clear()
        }
        //?}
    }
}
