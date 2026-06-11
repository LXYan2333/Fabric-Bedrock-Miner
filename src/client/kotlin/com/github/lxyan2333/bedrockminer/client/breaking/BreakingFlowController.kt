package com.github.lxyan2333.bedrockminer.client.breaking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import com.github.lxyan2333.bedrockminer.client.message.Messager

object BreakingFlowController {
    var enabled = false
        get() = field
        set(value) {
            field = value
        }

    private var scope: CoroutineScope? = null
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
        Messager.actionBar("Bedrock Miner started!")
    }

    fun disable() {
        if (!enabled) return
        enabled = false
        Messager.actionBar("Bedrock Miner stopped.")
        scope?.cancel()
        scope = null
        activeFlows.clear()
    }

    fun tryEnqueueBlock(pos: BlockPos) {
        if (!enabled) return
        if (isPositionProtected(pos)) return
        if (activeFlows.any { it.targetPos == pos }) return
        val flow = BreakingFlow(pos)
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