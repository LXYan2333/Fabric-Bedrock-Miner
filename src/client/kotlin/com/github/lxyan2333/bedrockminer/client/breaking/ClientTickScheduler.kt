package com.github.lxyan2333.bedrockminer.client.breaking

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object ClientTickScheduler {
    private val continuations = mutableListOf<CancellableContinuation<Unit>>()

    fun onTick() {
        val pending = continuations.toList()
        continuations.clear()
        pending.forEach { it.resume(Unit) }
    }

    suspend fun awaitTicks(count: Int = 1) {
        repeat(count) {
            suspendCancellableCoroutine<Unit> { cont ->
                continuations.add(cont)
            }
        }
    }
}