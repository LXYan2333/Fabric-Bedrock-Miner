package com.github.lxyan2333.bedrockminer.client.config

import com.github.lxyan2333.bedrockminer.client.breaking.BreakingFlowController
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fi.dy.masa.malilib.MaLiLibReference
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.IConfigHandler
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed
import fi.dy.masa.malilib.config.options.ConfigOptionList
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.hotkeys.IKeybindManager
import fi.dy.masa.malilib.hotkeys.IKeybindProvider
import fi.dy.masa.malilib.util.StringUtils
import fi.dy.masa.malilib.util.data.json.JsonUtils
import java.nio.file.Files
import kotlin.io.path.exists

object Configs : IConfigHandler, IKeybindProvider {
    private val configFile = MaLiLibReference.CONFIG_DIR.resolve("bedrock-miner.json")

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

    val options: List<IConfigBase> = listOf(BEDROCK_MINER_ENABLED, APPROACH_MODE)

    override fun load() {
        if (!Files.exists(configFile)) return
        try {
            val element = JsonParser.parseReader(Files.newBufferedReader(configFile))
            if (element.isJsonObject) {
                ConfigUtils.readConfigBase(element.asJsonObject, "Generic", options)
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
            ConfigUtils.writeConfigBase(root, "Generic", options)
            JsonUtils.writeJsonToFile(root, configFile)
        } catch (_: Exception) {
        }
    }

    override fun onConfigsChanged() {
        super.onConfigsChanged()
        BreakingFlowController.cancelAllFlows()
    }

    override fun addHotkeys(manager: IKeybindManager?) {
        manager?.addHotkeysForCategory("bedrock-miner", "bedrock-miner.hotkeys.generic", listOf(BEDROCK_MINER_ENABLED))
    }

    override fun addKeysToMap(manager: IKeybindManager?) {
        manager?.addKeybindToMap(BEDROCK_MINER_ENABLED.keybind)
    }

    fun init() {
        InputEventHandler.getKeybindManager()
            .registerKeybindProvider(this)
    }
}