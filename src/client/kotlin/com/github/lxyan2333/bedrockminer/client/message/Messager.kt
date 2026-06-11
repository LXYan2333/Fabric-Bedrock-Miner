package com.github.lxyan2333.bedrockminer.client.message

import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.util.InfoUtils.showGuiOrActionBarMessage
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object Messager {
    fun actionBar(message: String) {
        showGuiOrActionBarMessage(Message.MessageType.INFO, message)
    }

    fun chat(message: String) {
        Minecraft.getInstance().gui.chat.addClientSystemMessage(Component.literal(message))
    }
}