package com.github.lxyan2333.bedrockminer.client.event

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.world.InteractionResult
import com.github.lxyan2333.bedrockminer.client.area.AreaRestriction
import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.breaking.ClientTickScheduler
import com.github.lxyan2333.bedrockminer.client.config.AllowOrBlockMode
import com.github.lxyan2333.bedrockminer.client.config.Configs
import com.github.lxyan2333.bedrockminer.client.config.ClientConfigHandler
import com.github.lxyan2333.bedrockminer.client.message.Messager
import com.github.lxyan2333.bedrockminer.client.network.ClientNetworkHandler
import com.github.lxyan2333.bedrockminer.client.render.AreaRenderer
import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import fi.dy.masa.malilib.config.ConfigManager
import fi.dy.masa.malilib.event.RenderEventHandler
import fi.dy.masa.malilib.util.StringUtils
//? if >= 26.2 {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents
//?} else
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

object ClientEventHandlers {
    //? if < 26.2
    //private var lastLevel: ClientLevel? = null

    private fun isBlockAllowed(blockState: BlockState): Boolean {
        val blockId = IdentifierCompat.blockId(blockState.block).toString()

        // Always block special blocks unless server explicitly allows them
        val isIntegratedServer = Minecraft.getInstance().singleplayerServer != null
        if (!ServerConfigData.serverHasMod && !isIntegratedServer && ServerConfigData.SPECIAL_BLOCKS.contains(blockId)) {
            Messager.actionBar(StringUtils.translate("bedrockminer.message.restricted.server_special_block", blockId))
            return false
        }

        if (ServerConfigData.serverHasMod && !isIntegratedServer) {
            // Server block list takes precedence
            if (ServerConfigData.serverBlockList.contains(blockId)) {
                Messager.actionBar(StringUtils.translate("bedrockminer.message.restricted.server_block_list", blockId))
                return false
            }

            // Server allow list
            if (!ServerConfigData.serverAllowList.contains(blockId)) {
                if (ServerConfigData.serverBlockListMode == "BLOCKED") {
                    Messager.actionBar(
                        StringUtils.translate(
                            "bedrockminer.message.restricted.server_allow_list",
                            blockId
                        )
                    )
                    return false
                }
            }
        }

        // Client allow list
        if (Configs.Client.ALLOW_LIST.strings.contains(blockId)) {
            return true
        }

        // Client block list
        if (Configs.Client.BLOCK_LIST.strings.contains(blockId)) {
            return false
        }

        // client mode
        val clientMode = Configs.Client.AlloeOrBlockMode.optionListValue as AllowOrBlockMode
        when (clientMode) {
            AllowOrBlockMode.BLOCKED -> return false
            AllowOrBlockMode.ALLOWED -> return true
        }
    }

    private fun showBedrockRightClickHint() {
        //? if >=1.18 {
        val toggleHotkey = Configs.Generic.BEDROCK_MINER_ENABLED.keybind.keysDisplayString
        //?} else
        //val toggleHotkey = Configs.Generic.BEDROCK_MINER_ENABLE_HOTKEY.keybind.keysDisplayString
        val configHotkey = Configs.Generic.OPEN_CONFIG_GUI.keybind.keysDisplayString
        Messager.actionBar(
            StringUtils.translate(
                "bedrockminer.message.bedrock_right_click_hint",
                toggleHotkey,
                configHotkey
            )
        )
    }

    fun register() {
        ConfigManager.getInstance().registerConfigHandler("bedrock-miner", Configs)
        Configs.init()
        RenderEventHandler.getInstance().registerWorldLastRenderer(AreaRenderer)
        ModNetwork.registerPayloadTypes()
        ClientNetworkHandler.registerClientHandlers()

        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            if (world.isClientSide && player.mainHandItem.isEmpty && world.getBlockState(hitResult.blockPos).block == Blocks.BEDROCK) {
                showBedrockRightClickHint()
            }
            InteractionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            if (!world.isClientSide) {
                return@register InteractionResult.PASS
            }
            if (!BreakingFlowController.enabled) {
                return@register InteractionResult.PASS
            }
            if (!BreakingFlowController.isInternalBreak && BreakingFlowController.isPositionProtected(pos)) {
                return@register InteractionResult.FAIL
            }
            val blockState = world.getBlockState(pos)
            if (!isBlockAllowed(blockState)) {
                return@register InteractionResult.PASS
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
                return@register InteractionResult.PASS
            }
            BreakingFlowController.tryEnqueueBlock(pos)
            return@register InteractionResult.FAIL
        }

        //? if >= 26.2 {
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register { _, _ -> BreakingFlowController.cancelAllFlows() }
        //? } else {
        /*ClientTickEvents.START_CLIENT_TICK.register { client ->
            val currentLevel = client.level ?: return@register
            if (currentLevel != lastLevel) {
                lastLevel = currentLevel
                BreakingFlowController.cancelAllFlows()
            }
        }
        *///?}

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            if (Configs.Generic.BEDROCK_MINER_ENABLED.booleanValue) {
                BreakingFlowController.enable()
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            BreakingFlowController.onDisconnect()
            ClientConfigHandler.resetOnDisconnect()
        }
    }
}
