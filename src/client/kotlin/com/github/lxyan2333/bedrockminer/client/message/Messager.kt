package com.github.lxyan2333.bedrockminer.client.message

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object Messager {
    fun actionBar(message: String) {
        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(message), false)
    }

    fun chat(message: String) {
        Minecraft.getInstance().gui.chat.addClientSystemMessage(Component.literal(message))
    }
}