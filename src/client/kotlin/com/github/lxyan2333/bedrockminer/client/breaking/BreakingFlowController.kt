package com.github.lxyan2333.bedrockminer.client.breaking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import com.github.lxyan2333.bedrockminer.client.message.Messager

object BreakingFlowController {
    @Volatile
    private var enabled = true

    private var scope: CoroutineScope? = null

    fun isEnabled(): Boolean = enabled

    fun toggle() {
        if (enabled) disable() else enable()
    }

    fun enable() {
        if (enabled) return
        enabled = true
        Messager.actionBar("Bedrock Miner started!")
        startConsumer()
    }

    fun disable() {
        if (!enabled) return
        enabled = false
        Messager.actionBar("Bedrock Miner stopped.")
        scope?.cancel()
        scope = null
    }

    fun enqueueBlock(pos: BlockPos) {
        if (!enabled) return
        scope?.launch {
            BreakingFlow(pos).execute()
        }
    }

    fun onDisconnect() {
        disable()
    }

    fun startConsumer() {
        scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
    }
}