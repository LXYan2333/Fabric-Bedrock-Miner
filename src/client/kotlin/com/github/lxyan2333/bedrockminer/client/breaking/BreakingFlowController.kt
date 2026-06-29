package com.github.lxyan2333.bedrockminer.client.breaking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import com.github.lxyan2333.bedrockminer.client.message.Messager
import net.minecraft.world.level.block.state.BlockState
import fi.dy.masa.malilib.util.StringUtils

object BreakingFlowController {
    var enabled = false
        get() = field
        set(value) {
            field = value
        }

    var scope: CoroutineScope? = null
    val activeFlows = mutableSetOf<BreakingFlow>()
    var isInternalBreak = false
        get() = field
        set(value) {
            field = value
        }

    fun isPositionProtected(pos: BlockPos): Boolean =
        activeFlows.any { it.currentApproach?.occupies(pos) == true }

    fun toggle() {
        if (enabled) disable() else enable()
    }

    fun enable() {
        if (scope == null) startConsumer()
        if (enabled) return
        enabled = true
        Messager.actionBar(StringUtils.translate("bedrockminer.message.started"))
    }

    fun disable() {
        if (!enabled) return
        enabled = false
        activeFlows.forEach { it.doCleanUp = false }
        Messager.actionBar(StringUtils.translate("bedrockminer.message.stopped"))
        scope?.cancel()
        scope = null
        activeFlows.clear()
    }

    fun tryEnqueueBlock(pos: BlockPos) {
        if (!enabled) return
        if (isPositionProtected(pos)) return
        val level = Minecraft.getInstance().level ?: return
        val blockState = level.getBlockState(pos)
        val flow = BreakingFlow(pos, blockState)
        activeFlows.add(flow)
        scope?.launch {
            try {
                flow.execute()
            } finally {
                activeFlows.remove(flow)
            }
        }
    }

    fun cancelAllFlows() {
        activeFlows.forEach { it.doCleanUp = false }
        scope?.cancel()
        scope = null
        activeFlows.clear()
        if (enabled) startConsumer()
    }

    fun onDisconnect() {
        cancelAllFlows()
    }

    fun startConsumer() {
        scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
    }
}