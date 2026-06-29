package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.config.Configs
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
//? if >=1.18 {
import net.minecraft.client.renderer.chunk.RenderRegionCache
//?}
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

        val mc = Minecraft.getInstance()
        //? if <1.20.2 {
        /*try {
            val renderer = mc.levelRenderer
            val viewArea = renderer.viewArea ?: return
            val dispatcher = renderer.chunkRenderDispatcher ?: return
            //? if >=1.18 {
            val cache = RenderRegionCache()
            //?}

            for (pos in pendingUpdates) {
                val section = viewArea.getRenderChunkAt(pos) ?: continue
                //? if >=1.18 {
                dispatcher.rebuildChunkSync(section, cache)
                //?} else
                //dispatcher.rebuildChunkSync(section)
                section.setNotDirty()
            }
        } finally {
            pendingUpdates.clear()
        }
        *///?} else if < 26.2 {
        /*try {
            val renderer = mc.levelRenderer
            val viewArea = renderer.viewArea ?: return
            val dispatcher = renderer.sectionRenderDispatcher ?: return
            val cache = RenderRegionCache()

            val sections = pendingUpdates.map { e -> viewArea.getRenderSectionAt(e) }.filterNotNull().distinct()
            for (section in sections) {
                dispatcher.rebuildSectionSync(section, cache)
                section.setNotDirty()
            }
        } finally {
            pendingUpdates.clear()
        }
        *///?} else {
        try {
            val renderer = mc.levelRenderer
            val viewArea = renderer.viewArea() ?: return
            val cache = RenderRegionCache()
            val level = mc.level ?: return

            val sections = pendingUpdates.map { e -> viewArea.getRenderSectionAt(e) }.filterNotNull().distinct()

            // NOTE: I do not know why ghost block appears, and I do not know why this can fix ghost block
            // I talked with ChatGPT a lot, investigated a lot, but I found nothing useful.
            BreakingFlowController.scope?.launch {
                ClientTickScheduler.awaitTicks(2)
                for (section in sections) {
                    section.lastCompileTask?.cancel()
                    println("before my call to compileSync")
                    section.compileAsync(cache.createRegion(level, section.sectionNode))
                }
            }
        } finally {
            pendingUpdates.clear()
        }
        //?}

    }
}
