package com.github.lxyan2333.bedrockminer.client.config

import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.github.lxyan2333.bedrockminer.client.compat.modmenu.GuiConfigs
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fi.dy.masa.malilib.MaLiLibReference
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.IConfigHandler
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.config.options.ConfigOptionList
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.gui.GuiBase
import fi.dy.masa.malilib.hotkeys.IKeybindManager
import fi.dy.masa.malilib.hotkeys.IKeybindProvider
import fi.dy.masa.malilib.util.StringUtils
import fi.dy.masa.malilib.util.data.json.JsonUtils
import java.nio.file.Files
import kotlin.io.path.exists

object Configs : IConfigHandler, IKeybindProvider {
    private val configFile = MaLiLibReference.CONFIG_DIR.resolve("bedrock-miner.json")

    private val GENERIC_KEY = "bedrockminer.config.generic"

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
        ).apply { setComment(StringUtils.translate("bedrockminer.config.approach.comment")) }

        val OPEN_CONFIG_GUI: ConfigHotkey = ConfigHotkey(
            "openConfigGui",
            "LEFT_ALT,B,C",
        ).apply {
            keybind.setCallback { _, _ ->
                GuiBase.openGui(GuiConfigs())
                true
            }
        }

        val OPTIONS: List<IConfigBase> = listOf(BEDROCK_MINER_ENABLED, APPROACH_MODE, OPEN_CONFIG_GUI)
    }

    private val CLIENT_KEY = "bedrockminer.config.client"

    object Client {
        // Placeholder for future client-side configs
        val OPTIONS: List<IConfigBase> = listOf()
    }

    private val SERVER_KEY = "bedrockminer.config.server"

    object Server {
        // Placeholder for future server-side configs
        val OPTIONS: List<IConfigBase> = listOf()
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
        manager?.addHotkeysForCategory("bedrock-miner", "bedrock-miner.hotkeys.generic", listOf(Generic.BEDROCK_MINER_ENABLED, Generic.OPEN_CONFIG_GUI))
    }

    override fun addKeysToMap(manager: IKeybindManager?) {
        manager?.addKeybindToMap(Generic.BEDROCK_MINER_ENABLED.keybind)
        manager?.addKeybindToMap(Generic.OPEN_CONFIG_GUI.keybind)
    }

    fun init() {
        InputEventHandler.getKeybindManager()
            .registerKeybindProvider(this)
    }
}
