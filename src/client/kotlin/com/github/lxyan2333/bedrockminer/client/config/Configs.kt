package com.github.lxyan2333.bedrockminer.client.config

import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.compat.modmenu.GuiConfigs
import com.github.lxyan2333.bedrockminer.client.message.Messager
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.common.collect.ImmutableList
import fi.dy.masa.malilib.MaLiLibReference
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.IConfigHandler
import fi.dy.masa.malilib.config.options.ConfigBlockState
import fi.dy.masa.malilib.config.options.ConfigBoolean
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.config.options.ConfigInteger
import fi.dy.masa.malilib.config.options.ConfigOptionList
import fi.dy.masa.malilib.config.options.ConfigStringList
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.gui.GuiBase
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper
import fi.dy.masa.malilib.hotkeys.IKeybindManager
import fi.dy.masa.malilib.hotkeys.IKeybindProvider
import fi.dy.masa.malilib.util.StringUtils
import fi.dy.masa.malilib.util.data.json.JsonUtils
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SupportType
import java.nio.file.Files
import kotlin.io.path.exists

object Configs : IConfigHandler, IKeybindProvider {
    private val configFile = MaLiLibReference.CONFIG_DIR.resolve("bedrock-miner.json")

    object Generic {
        val BEDROCK_MINER_ENABLED: ConfigBooleanHotkeyed = ConfigBooleanHotkeyed(
            "toggleEnabled",
            false,
            "LEFT_ALT,B,M",
            StringUtils.translate("bedrockminer.config.toggle_enabled.comment"),
        ).apply {
            setValueChangeCallback { v ->
                if (v.booleanValue) {
                    BreakingFlowController.enable()
                } else {
                    BreakingFlowController.disable()
                }
            }
        }

        val APPROACH_MODE: ConfigOptionList = ConfigOptionList(
            "approachMode",
            ApproachMode.VANILLA_FAST,
            StringUtils.translate("bedrockminer.config.approach.comment"),
        )

        val OPEN_CONFIG_GUI: ConfigHotkey = ConfigHotkey(
            "openConfigGui",
            "LEFT_ALT,B,C",
            StringUtils.translate("bedrockminer.config.opengui.comment"),
        ).apply {
            keybind.setCallback { _, _ ->
                GuiBase.openGui(GuiConfigs())
                true
            }
        }

        val MAX_RETRIES: ConfigInteger = ConfigInteger(
            "maxRetries",
            2, 1, 100,
            StringUtils.translate("bedrockminer.config.max_retries.comment"),
        )

        val REMOVE_GHOST_BLOCKS: ConfigBoolean = ConfigBoolean(
            "removeGhostBlocks",
            true,
            StringUtils.translate("bedrockminer.config.remove_ghost_blocks.comment"),
        )

        val SKIP_INSTANT_MINE_CHECK: ConfigBoolean = ConfigBoolean(
            "skipInstantMineCheck",
            false,
            StringUtils.translate("bedrockminer.config.skip_instant_mine_check.comment"),
        )

        val SUPPORT_BLOCK: ConfigBlockState = ConfigBlockState(
            "supportBlock",
            Blocks.SLIME_BLOCK.defaultBlockState(),
            StringUtils.translate("bedrockminer.config.support_block.comment"),
        ).apply {
            setValueChangeCallback { config ->
                config.blockStateValue.cache?.let {
                    if (!it.isFaceSturdy(Direction.UP, SupportType.CENTER)) {
                        config.setBlockStateValue(config.lastBlockStateValue)
                        Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_support_block"))
                    }
                }
            }
        }

        val OPTIONS: List<IConfigBase> = listOf(
            BEDROCK_MINER_ENABLED,
            APPROACH_MODE,
            OPEN_CONFIG_GUI,
            MAX_RETRIES,
            SUPPORT_BLOCK,
            REMOVE_GHOST_BLOCKS,
            SKIP_INSTANT_MINE_CHECK,
        )
    }

    object Client {
        val BLOCK_LIST: ConfigStringList = ConfigStringList(
            "blockList",
            ImmutableList.of(),
            StringUtils.translate("bedrockminer.config.blockList.comment"),
        ).apply {
            setValueChangeCallback { config ->
                val invalid = config.strings.firstOrNull { !isValidBlockName(it) }
                if (invalid != null) {
                    Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_block_name", invalid))
                    config.setStrings(config.lastStringListValue)
                }
            }
        }

        val ALLOW_LIST: ConfigStringList = ConfigStringList(
            "allowList",
            ImmutableList.of("minecraft:bedrock"),
            StringUtils.translate("bedrockminer.config.allowList.comment"),
        ).apply {
            setValueChangeCallback { config ->
                val invalid = config.strings.firstOrNull { !isValidBlockName(it) }
                if (invalid != null) {
                    Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_block_name", invalid))
                    config.setStrings(config.lastStringListValue)
                }
            }
        }

        val AlloeOrBlockMode: ConfigOptionList = ConfigOptionList(
            "blockListMode",
            AllowOrBlockMode.BLOCKED,
            StringUtils.translate("bedrockminer.config.alloworblockmode.comment"),
        )

        val OPTIONS: List<IConfigBase> = listOf(BLOCK_LIST, ALLOW_LIST, AlloeOrBlockMode)
    }

    object Server {
        val BLOCK_LIST: ConfigStringList = ConfigStringList(
            "blockList",
            ImmutableList.of(),
            StringUtils.translate("bedrockminer.config.server.blockList.comment"),
        )

        val ALLOW_LIST: ConfigStringList = ConfigStringList(
            "allowList",
            ImmutableList.of(),
            StringUtils.translate("bedrockminer.config.server.allowList.comment"),
        )

        val AllowBlockMode: ConfigOptionList = ConfigOptionList(
            "blockListMode",
            AllowOrBlockMode.BLOCKED,
            StringUtils.translate("bedrockminer.config.server.alloworblockmode.comment"),
        )

        val WAIT_SERVER_TICK_PLAYER_ENTITY_TICKS: ConfigInteger = ConfigInteger(
            "waitServerTickPlayerEntityTicks",
            2, 0, 100,
            StringUtils.translate("bedrockminer.config.server.waitServerTickPlayerEntityTicks.comment"),
        )

        val OPTIONS: List<IConfigBase> =
            listOf(WAIT_SERVER_TICK_PLAYER_ENTITY_TICKS, BLOCK_LIST, ALLOW_LIST, AllowBlockMode)

        val ALL_OPTIONS_WRAPPER: List<ConfigOptionWrapper> = listOf(
            ConfigOptionWrapper(WAIT_SERVER_TICK_PLAYER_ENTITY_TICKS),
            ConfigOptionWrapper("bedrockminer.config.server.note"),
            ConfigOptionWrapper(BLOCK_LIST),
            ConfigOptionWrapper(ALLOW_LIST),
            ConfigOptionWrapper(AllowBlockMode),
        )

    }

    override fun load() {
        if (!Files.exists(configFile)) return
        try {
            val element = JsonParser.parseReader(Files.newBufferedReader(configFile))
            if (element.isJsonObject) {
                ConfigUtils.readConfigBase(element.asJsonObject, "Generic", Generic.OPTIONS)
                ConfigUtils.readConfigBase(element.asJsonObject, "Client", Client.OPTIONS)
                ConfigUtils.readConfigBase(element.asJsonObject, "Server", Server.OPTIONS)
            }
        } catch (_: Exception) {
        }
    }

    override fun save() {
        try {
            if (!MaLiLibReference.CONFIG_DIR.exists()) {
                Files.createDirectories(MaLiLibReference.CONFIG_DIR)
            }
            val root = JsonObject()
            ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS)
            ConfigUtils.writeConfigBase(root, "Client", Client.OPTIONS)
            ConfigUtils.writeConfigBase(root, "Server", Server.OPTIONS)
            JsonUtils.writeJsonToFile(root, configFile)
        } catch (_: Exception) {
        }
    }

    override fun onConfigsChanged() {
        super.onConfigsChanged()
        BreakingFlowController.cancelAllFlows()
    }

    override fun addHotkeys(manager: IKeybindManager?) {
        manager?.addHotkeysForCategory(
            "bedrock-miner",
            "bedrock-miner.hotkeys.generic",
            listOf(Generic.BEDROCK_MINER_ENABLED, Generic.OPEN_CONFIG_GUI)
        )
    }

    override fun addKeysToMap(manager: IKeybindManager?) {
        manager?.addKeybindToMap(Generic.BEDROCK_MINER_ENABLED.keybind)
        manager?.addKeybindToMap(Generic.OPEN_CONFIG_GUI.keybind)
    }

    fun init() {
        InputEventHandler.getKeybindManager().registerKeybindProvider(this)
    }

    private fun isValidBlockName(name: String): Boolean {
        val key = net.minecraft.resources.Identifier.tryParse(name) ?: return false
        return BuiltInRegistries.BLOCK.containsKey(key)
    }
}
