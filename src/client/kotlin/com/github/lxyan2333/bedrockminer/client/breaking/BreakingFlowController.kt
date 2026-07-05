package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.area.AreaRestriction
import com.github.lxyan2333.bedrockminer.client.config.AllowOrBlockMode
import com.github.lxyan2333.bedrockminer.client.config.Configs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import com.github.lxyan2333.bedrockminer.client.message.Messager
import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import net.minecraft.world.level.block.state.BlockState
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.world.InteractionResult

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

    fun isPositionProtected(pos: BlockPos): Boolean = activeFlows.any { it.currentApproach?.occupies(pos) == true }

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

    private fun listContains(list: List<String>, blockState: BlockState): Boolean {
        val blockId = IdentifierCompat.blockId(blockState.block).toString()
        if (list.contains(blockId)) {
            return true
        }
        return list.any { it.equals(blockState.block.name.string, ignoreCase = true) }
    }

    private fun isBlockTypeAllowed(blockState: BlockState): Boolean {
        val blockId = IdentifierCompat.blockId(blockState.block).toString()

        // Always block special blocks unless server explicitly allows them
        val isIntegratedServer = Minecraft.getInstance().singleplayerServer != null
        if (!ServerConfigData.serverHasMod && !isIntegratedServer && ServerConfigData.SPECIAL_BLOCKS.contains(blockId)) {
            Messager.actionBar(StringUtils.translate("bedrockminer.message.restricted.server_special_block", blockState.block.name.string))
            return false
        }

        if (ServerConfigData.serverHasMod && !isIntegratedServer) {
            // Server block list takes precedence
            if (ServerConfigData.serverBlockList.contains(blockId)) {
                Messager.actionBar(StringUtils.translate("bedrockminer.message.restricted.server_block_list", blockState.block.name.string))
                return false
            }

            // Server allow list
            if (!ServerConfigData.serverAllowList.contains(blockId)) {
                if (ServerConfigData.serverBlockListMode == "BLOCKED") {
                    Messager.actionBar(
                        StringUtils.translate(
                            "bedrockminer.message.restricted.server_allow_list", blockState.block.name.string
                        )
                    )
                    return false
                }
            }
        }

        // Client allow list
        if (Configs.Client.ALLOW_LIST.strings.let { listContains(it, blockState) }) {
            return true
        }

        // Client block list
        if (Configs.Client.BLOCK_LIST.strings.let { listContains(it, blockState) }) {
            return false
        }

        // client mode
        val clientMode = Configs.Client.AlloeOrBlockMode.optionListValue as AllowOrBlockMode
        when (clientMode) {
            AllowOrBlockMode.BLOCKED -> return false
            AllowOrBlockMode.ALLOWED -> return true
        }
    }


    fun tryEnqueueBlock(pos: BlockPos): InteractionResult {
        val level = Minecraft.getInstance().level ?: return InteractionResult.PASS

        if (!enabled) {
            return InteractionResult.PASS
        }
        if (isInternalBreak) {
            return InteractionResult.PASS
        }
        if (isPositionProtected(pos)) {
            return InteractionResult.FAIL
        }
        val blockState = level.getBlockState(pos)
        if (!isBlockTypeAllowed(blockState)) {
            return InteractionResult.PASS
        }
        if (!AreaRestriction.isPositionAllowed(pos)) {
            Messager.actionBar(
                StringUtils.translate(
                    "bedrockminer.message.restricted.area",
                    pos.x,
                    pos.y,
                    pos.z,
                )
            )
            return InteractionResult.PASS
        }

        val flow = BreakingFlow(pos, blockState)
        activeFlows.add(flow)
        scope?.launch {
            try {
                flow.execute()
            } finally {
                activeFlows.remove(flow)
            }
        }
        return InteractionResult.FAIL
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