package com.github.lxyan2333.bedrockminer.client.message

import com.github.lxyan2333.bedrockminer.client.compat.MinecraftClientCompat
import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.util.InfoUtils.showGuiOrActionBarMessage
//? if >= 26.1.2
import fi.dy.masa.malilib.util.game.wrap.GameWrap.printToChat

object Messager {
    fun actionBar(message: String) {
        showGuiOrActionBarMessage(Message.MessageType.INFO, message)
    }

    fun chat(message: String) {
        //? if >= 26.1.2 {
        printToChat(message)
        //?} else
        //MinecraftClientCompat.addChatMessage(MinecraftClientCompat.literal(message))
    }
}
