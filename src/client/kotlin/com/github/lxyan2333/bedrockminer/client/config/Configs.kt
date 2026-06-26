package com.github.lxyan2333.bedrockminer.client.config

import com.github.lxyan2333.bedrockminer.client.area.AreaRestriction
import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.compat.modmenu.GuiConfigs
import com.github.lxyan2333.bedrockminer.client.message.Messager
import com.github.lxyan2333.bedrockminer.compat.GsonCompat
import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
import com.google.gson.JsonObject
import com.google.common.collect.ImmutableList
import fi.dy.masa.malilib.MaLiLibReference
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.IConfigHandler
//? if >=1.21.11 {
import fi.dy.masa.malilib.config.options.ConfigBlockState
//?} else
//import fi.dy.masa.malilib.config.options.ConfigString
import fi.dy.masa.malilib.config.options.ConfigBoolean
//? if >=1.18 {
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed
//?}
import fi.dy.masa.malilib.config.options.ConfigColor
//? if >=1.20.5 {
import fi.dy.masa.malilib.config.options.ConfigFloat
//?} else
//import fi.dy.masa.malilib.config.options.ConfigDouble
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
//? if <1.20.5
//import fi.dy.masa.malilib.util.FileUtils
//? if >=1.21.11 {
import fi.dy.masa.malilib.util.data.json.JsonUtils
//?} else
//import fi.dy.masa.malilib.util.JsonUtils
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SupportType
//? if <1.20.5
//import java.io.File
import java.nio.file.Files
import kotlin.io.path.exists

object Configs : IConfigHandler, IKeybindProvider {
    //? if >=1.20.5 {
    private val configFile = MaLiLibReference.CONFIG_DIR.resolve("bedrock-miner.json")
    //?} else
    //private val configFile = File(FileUtils.getConfigDirectory(), "bedrock-miner.json")

    object Generic {
        //? if >=1.18 {
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
        //?} else {
        /*val BEDROCK_MINER_ENABLED: ConfigBoolean = ConfigBoolean(
            "toggleEnabled",
            false,
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

        val BEDROCK_MINER_ENABLE_HOTKEY: ConfigHotkey = ConfigHotkey(
            "toggleEnabledHotkey",
            "LEFT_ALT,B,M",
            StringUtils.translate("bedrockminer.config.toggle_enabled.comment"),
        ).apply {
            keybind.setCallback { _, _ ->
                BEDROCK_MINER_ENABLED.booleanValue = !BEDROCK_MINER_ENABLED.booleanValue
                true
            }
        }
        *///?}

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

        val WAIT_TICKS: ConfigInteger = ConfigInteger(
            "waitTicks",
            20, 1, 200,
            StringUtils.translate("bedrockminer.config.wait_ticks.comment"),
        )

        //? if >=1.21.11 {
        val SUPPORT_BLOCK: ConfigBlockState = ConfigBlockState(
            "supportBlock",
            Blocks.SLIME_BLOCK.defaultBlockState(),
            StringUtils.translate("bedrockminer.config.support_block.comment"),
        ).apply {
            setValueChangeCallback { config ->
                config.blockStateValue.cache?.let {
                    if (!isValidSupportBlock(config.blockStateValue.block)) {
                        config.setBlockStateValue(config.lastBlockStateValue)
                        Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_support_block"))
                    }
                }
            }
        }
        //?} else {
        /*val SUPPORT_BLOCK: ConfigString = ConfigString(
            "supportBlock",
            "minecraft:slime_block",
            StringUtils.translate("bedrockminer.config.support_block.comment"),
        ).apply {
            setValueChangeCallback { config ->
                val block = blockFromName(config.stringValue)
                if (block == null || !isValidSupportBlock(block)) {
                    config.setValueFromString(config.defaultStringValue)
                    Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_support_block"))
                }
            }
        }
        *///?}

        val supportBlock: Block
            get() {
                //? if >=1.21.11 {
                return SUPPORT_BLOCK.blockStateValue.block
                //?} else
                //return blockFromName(SUPPORT_BLOCK.stringValue)?.takeIf(::isValidSupportBlock) ?: Blocks.SLIME_BLOCK
            }

        val OPTIONS: List<IConfigBase> = listOf(
            BEDROCK_MINER_ENABLED,
            //? if <1.18
            //BEDROCK_MINER_ENABLE_HOTKEY,
            APPROACH_MODE,
            OPEN_CONFIG_GUI,
            MAX_RETRIES,
            WAIT_TICKS,
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
                val invalid = config.strings.firstOrNull { if (it == "") false else !isValidBlockName(it) }
                if (invalid != null) {
                    Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_block_name", invalid))
                    //? if >=1.21.11 {
                    config.setStrings(config.lastStringListValue)
                    //?} else
                    //config.setStrings(config.defaultStrings)
                }
            }
        }

        val ALLOW_LIST: ConfigStringList = ConfigStringList(
            "allowList",
            ImmutableList.of("minecraft:bedrock"),
            StringUtils.translate("bedrockminer.config.allowList.comment"),
        ).apply {
            setValueChangeCallback { config ->
                val invalid = config.strings.firstOrNull { if (it == "") false else !isValidBlockName(it) }
                if (invalid != null) {
                    Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_block_name", invalid))
                    //? if >=1.21.11 {
                    config.setStrings(config.lastStringListValue)
                    //?} else
                    //config.setStrings(config.defaultStrings)
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

    object Area {
        val AREA_RESTRICTION_ENABLED: ConfigBoolean = ConfigBoolean(
            "areaRestrictionEnabled",
            false,
            StringUtils.translate("bedrockminer.config.area.area_restriction_enabled.comment"),
        )

        val RESTRICT_MINING_AREA: ConfigStringList = ConfigStringList(
            "restrictMiningArea",
            ImmutableList.of(),
            StringUtils.translate("bedrockminer.config.area.restrict_mining_area.comment"),
        ).apply {
            setValueChangeCallback { config ->
                val invalid = config.strings.firstOrNull { it.isNotEmpty() && !AreaRestriction.isValidArea(it) }
                if (invalid != null) {
                    Messager.actionBar(StringUtils.translate("bedrockminer.message.invalid_area", invalid))
                    //? if >=1.21.11 {
                    config.setStrings(config.lastStringListValue)
                    //?} else
                    //config.setStrings(config.defaultStrings)
                }
            }
        }

        val AREA_BOX_COLOR: ConfigColor = ConfigColor(
            "areaBoxColor",
            "#FF40FF78",
            StringUtils.translate("bedrockminer.config.area.area_box_color.comment"),
        )

        val HIDE_AREA_BOX_BEHIND_BLOCKS: ConfigBoolean = ConfigBoolean(
            "hideAreaBoxBehindBlocks",
            false,
            StringUtils.translate("bedrockminer.config.area.hide_area_box_behind_blocks.comment"),
        )

        //? if >=1.20.5 {
        val AREA_BOX_LINE_WIDTH: ConfigFloat = ConfigFloat(
            "areaBoxLineWidth",
            2.0f,
            0.5f,
            8.0f,
            true,
            StringUtils.translate("bedrockminer.config.area.area_box_line_width.comment"),
        )
        //?} else {
        /*val AREA_BOX_LINE_WIDTH: ConfigDouble = ConfigDouble(
            "areaBoxLineWidth",
            2.0,
            0.5,
            8.0,
            true,
            StringUtils.translate("bedrockminer.config.area.area_box_line_width.comment"),
        )
        *///?}

        val areaBoxLineWidth: Float
            get() {
                //? if >=1.20.5 {
                return AREA_BOX_LINE_WIDTH.floatValue
                //?} else
                //return AREA_BOX_LINE_WIDTH.doubleValue.toFloat()
            }

        val OPTIONS: List<IConfigBase> = listOf(
            AREA_RESTRICTION_ENABLED,
            RESTRICT_MINING_AREA,
            AREA_BOX_COLOR,
            HIDE_AREA_BOX_BEHIND_BLOCKS,
            AREA_BOX_LINE_WIDTH,
        )
    }

    override fun load() {
        try {
            val element = GsonCompat.parseFile(configFile) ?: return
            if (element.isJsonObject) {
                ConfigUtils.readConfigBase(element.asJsonObject, "Generic", Generic.OPTIONS)
                ConfigUtils.readConfigBase(element.asJsonObject, "Client", Client.OPTIONS)
                ConfigUtils.readConfigBase(element.asJsonObject, "Server", Server.OPTIONS)
                ConfigUtils.readConfigBase(element.asJsonObject, "Area", Area.OPTIONS)
            }
        } catch (_: Exception) {
        }
    }

    override fun save() {
        try {
            //? if >=1.20.5 {
            if (!MaLiLibReference.CONFIG_DIR.exists()) {
                Files.createDirectories(MaLiLibReference.CONFIG_DIR)
            }
            //?} else
            //FileUtils.getConfigDirectory().mkdirs()
            val root = JsonObject()
            ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS)
            ConfigUtils.writeConfigBase(root, "Client", Client.OPTIONS)
            ConfigUtils.writeConfigBase(root, "Server", Server.OPTIONS)
            ConfigUtils.writeConfigBase(root, "Area", Area.OPTIONS)
            //? if >=1.21.11 {
            JsonUtils.writeJsonToFile(root, configFile)
            //?} else if >=1.20.5
            //JsonUtils.writeJsonToFileAsPath(root, configFile)
            //? if <1.20.5
            //JsonUtils.writeJsonToFile(root, configFile)
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
            listOf(
                //? if >=1.18 {
                Generic.BEDROCK_MINER_ENABLED,
                //?} else
                //Generic.BEDROCK_MINER_ENABLE_HOTKEY,
                Generic.OPEN_CONFIG_GUI,
            )
        )
    }

    override fun addKeysToMap(manager: IKeybindManager?) {
        //? if >=1.18 {
        manager?.addKeybindToMap(Generic.BEDROCK_MINER_ENABLED.keybind)
        //?} else
        //manager?.addKeybindToMap(Generic.BEDROCK_MINER_ENABLE_HOTKEY.keybind)
        manager?.addKeybindToMap(Generic.OPEN_CONFIG_GUI.keybind)
    }

    fun init() {
        InputEventHandler.getKeybindManager().registerKeybindProvider(this)
    }

    private fun isValidBlockName(name: String): Boolean {
        return IdentifierCompat.isKnownBlock(name)
    }

    private fun blockFromName(name: String): Block? {
        return IdentifierCompat.block(name)
    }

    private fun isValidSupportBlock(block: Block): Boolean {
        return block.defaultBlockState().cache?.isFaceSturdy(Direction.UP, SupportType.CENTER) == true
    }
}
