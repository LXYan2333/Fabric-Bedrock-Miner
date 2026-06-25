package com.github.lxyan2333.bedrockminer.client.compat.modmenu

import com.github.lxyan2333.bedrockminer.client.config.Configs
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.gui.button.IButtonActionListener
import fi.dy.masa.malilib.util.StringUtils

class GuiConfigs : GuiConfigsBase(10, 50, "bedrock-miner", null, "bedrockminer.gui.title.configs") {

    override fun initGui() {
        super.initGui()
        this.clearOptions()

        var x = 10
        val y = 26

        for (tab in ConfigGuiTab.entries) {
            val button = ButtonGeneric(x, y, -1, 20, tab.displayName)
            button.setEnabled(ConfigGuiState.currentTab != tab)
            this.addButton(button, ButtonListener(tab, this))
            x += button.width + 2
        }
    }

    override fun getConfigs(): List<ConfigOptionWrapper> {
        return when (ConfigGuiState.currentTab) {
            ConfigGuiTab.GENERIC -> ConfigOptionWrapper.createFor(Configs.Generic.OPTIONS)
            ConfigGuiTab.CLIENT -> ConfigOptionWrapper.createFor(Configs.Client.OPTIONS)
            ConfigGuiTab.SERVER -> Configs.Server.ALL_OPTIONS_WRAPPER
            ConfigGuiTab.AREA -> ConfigOptionWrapper.createFor(Configs.Area.OPTIONS)
        }
    }

    private class ButtonListener(
        private val tab: ConfigGuiTab,
        private val parent: GuiConfigs,
    ) : IButtonActionListener {
        override fun actionPerformedWithButton(button: ButtonBase?, mouseButton: Int) {
            ConfigGuiState.currentTab = tab
            this.parent.reCreateListWidget()
            this.parent.getListWidget()?.resetScrollbarPosition()
            this.parent.initGui()
        }
    }

    enum class ConfigGuiTab(val translationKey: String) {
        GENERIC("bedrockminer.gui.button.config_gui.generic"),
        AREA("bedrockminer.gui.button.config_gui.area"),
        CLIENT("bedrockminer.gui.button.config_gui.client"),
        SERVER("bedrockminer.gui.button.config_gui.server");

        val displayName: String
            get() = StringUtils.translate(translationKey)
    }
}

object ConfigGuiState {
    var currentTab: GuiConfigs.ConfigGuiTab = GuiConfigs.ConfigGuiTab.GENERIC
}
