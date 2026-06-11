package com.github.lxyan2333.bedrockminer.client.compat.modmenu

import com.github.lxyan2333.bedrockminer.client.config.Configs
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper

class GuiConfigs : GuiConfigsBase(10, 50, "bedrock-miner", null, "bedrockminer.gui.title.configs") {

    override fun getConfigs(): List<ConfigOptionWrapper> {
        val configs = Configs.options
        return ConfigOptionWrapper.createFor(configs)
    }
}