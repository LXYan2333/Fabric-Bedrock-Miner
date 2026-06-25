package com.github.lxyan2333.bedrockminer.client.compat

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
//? if >=26.1 {
import net.minecraft.world.inventory.ContainerInput
//?} else
//import net.minecraft.world.inventory.ClickType
import net.minecraft.world.entity.player.Player

object MinecraftClientCompat {
    fun swapInventorySlot(containerId: Int, slot: Int, hotbarSlot: Int, player: Player) {
        val gameMode = Minecraft.getInstance().gameMode ?: return
        //? if >=26.1 {
        gameMode.handleContainerInput(containerId, slot, hotbarSlot, ContainerInput.SWAP, player)
        //?} else
        //gameMode.handleInventoryMouseClick(containerId, slot, hotbarSlot, ClickType.SWAP, player)
    }

    fun addChatMessage(message: Component) {
        val chat = Minecraft.getInstance().gui.chat
        //? if >=26.1 {
        chat.addClientSystemMessage(message)
        //?} else
        //chat.addMessage(message)
    }
}
