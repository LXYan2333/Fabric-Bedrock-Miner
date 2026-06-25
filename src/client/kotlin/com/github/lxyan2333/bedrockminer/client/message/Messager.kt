package com.github.lxyan2333.bedrockminer.client.message

import com.github.lxyan2333.bedrockminer.client.compat.MinecraftClientCompat
import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.util.InfoUtils.showGuiOrActionBarMessage
import net.minecraft.network.chat.Component

object Messager {
    fun actionBar(message: String) {
        showGuiOrActionBarMessage(Message.MessageType.INFO, message)
    }

    fun chat(message: String) {
        MinecraftClientCompat.addChatMessage(Component.literal(message))
    }
}
