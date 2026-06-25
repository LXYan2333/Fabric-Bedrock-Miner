package com.github.lxyan2333.bedrockminer.client.compat

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
//? if <1.20.5
//import net.minecraft.world.phys.Vec3
//? if >=26.1 {
import net.minecraft.world.inventory.ContainerInput
//?} else
//import net.minecraft.world.inventory.ClickType
import net.minecraft.world.entity.player.Player

object MinecraftClientCompat {
    fun canInteractWithBlock(pos: BlockPos): Boolean {
        //? if <1.20.5 {
        //val player = Minecraft.getInstance().player ?: return true
        //return Vec3.atCenterOf(pos).distanceToSqr(player.eyePosition) <= 36.0
        //?} else {
        val player = Minecraft.getInstance().player ?: return true
        //? if >=1.21.11 {
        return player.isWithinBlockInteractionRange(pos, 0.0)
        //?} else
        //return player.canInteractWithBlock(pos, 0.0)
        //?}
    }

    fun rotationPacket(yaw: Float, pitch: Float, onGround: Boolean): ServerboundMovePlayerPacket.Rot {
        //? if >=1.21.11 {
        return ServerboundMovePlayerPacket.Rot(yaw, pitch, onGround, false)
        //?} else
        //return ServerboundMovePlayerPacket.Rot(yaw, pitch, onGround)
    }

    fun swapInventorySlot(containerId: Int, slot: Int, hotbarSlot: Int, player: Player) {
        val gameMode = Minecraft.getInstance().gameMode ?: return
        //? if >=26.1 {
        gameMode.handleContainerInput(containerId, slot, hotbarSlot, ContainerInput.SWAP, player)
        //?} else
        //gameMode.handleInventoryMouseClick(containerId, slot, hotbarSlot, ClickType.SWAP, player)
    }

    fun selectedSlot(inventory: Inventory): Int {
        //? if >=1.21.11 {
        return inventory.selectedSlot
        //?} else
        //return inventory.selected
    }

    fun setSelectedSlot(inventory: Inventory, slot: Int) {
        //? if >=1.21.11 {
        inventory.selectedSlot = slot
        //?} else
        //inventory.selected = slot
    }

    fun addChatMessage(message: Component) {
        val chat = Minecraft.getInstance().gui.chat
        //? if >=26.1 {
        chat.addClientSystemMessage(message)
        //?} else
        //chat.addMessage(message)
    }
}
