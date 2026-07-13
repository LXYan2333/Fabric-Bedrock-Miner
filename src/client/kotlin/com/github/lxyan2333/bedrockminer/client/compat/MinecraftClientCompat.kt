package com.github.lxyan2333.bedrockminer.client.compat

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.chat.Component
//? if <1.19
//import net.minecraft.network.chat.TextComponent
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.BlockHitResult
//? if >=26.1 {
import net.minecraft.world.inventory.ContainerInput
//?} else
//import net.minecraft.world.inventory.ClickType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

//? if <1.20
//import net.minecraft.world.level.material.Fluids

object MinecraftClientCompat {
    fun canInteractWithBlock(pos: BlockPos): Boolean {
        //? if <1.20.5 {
        /*val player = Minecraft.getInstance().player ?: return true
        return Vec3.atCenterOf(pos).distanceToSqr(eyePosition(player)) <= 36.0
        *///?} else {
        val player = Minecraft.getInstance().player ?: return true
        //? if >=1.21.11 {
        return player.isWithinBlockInteractionRange(pos, 0.0)
        //?} else
        //return player.canInteractWithBlock(pos, 0.0)
        //?}
    }

    fun eyePosition(player: Player): Vec3 {
        //? if >=1.17
        return player.eyePosition
        //? if <1.17
        //return player.getEyePosition(1.0f)
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

    fun getSelectedItem(inventory: Inventory): ItemStack {
        //? if > 1.21.1 {
        return inventory.selectedItem
        //?} else
        //return inventory.getSelected()
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

    //? if < 26.1.2 {
    /*fun addChatMessage(message: Component) {
        val chat = Minecraft.getInstance().gui.chat
        chat.addMessage(message)
    }
    *///?}

    fun literal(message: String): Component {
        //? if >=1.19
        return Component.literal(message)
        //? if <1.19
        //return TextComponent(message)
    }

    fun isOnGround(player: Player): Boolean {
        //? if >=1.20
        return player.onGround()
        //? if <1.20
        //return player.isOnGround
    }

    fun blockCenter(pos: BlockPos): Vec3 {
        return Vec3.atCenterOf(pos)
    }

    fun stackIs(stack: net.minecraft.world.item.ItemStack, item: net.minecraft.world.item.Item): Boolean {
        //? if >=1.17
        return stack.`is`(item)
        //? if <1.17
        //return stack.item == item
    }

    fun offset(pos: Vec3, direction: Direction, distance: Double): Vec3 {
        return pos.add(direction.stepX * distance, direction.stepY * distance, direction.stepZ * distance)
    }

    fun useItemOn(player: LocalPlayer, hand: InteractionHand, hitResult: BlockHitResult) {
        val gameMode = Minecraft.getInstance().gameMode ?: return
        //? if >=1.19
        gameMode.useItemOn(player, hand, hitResult)
        //? if <1.19
        //Minecraft.getInstance().level?.let { gameMode.useItemOn(player, it, hand, hitResult) }
    }

    fun canBeReplaced(level: Level, pos: BlockPos): Boolean {
        return canBeReplaced(level.getBlockState(pos))
    }

    fun canBeReplaced(state: BlockState): Boolean {
        //? if >=1.20
        return state.canBeReplaced()
        //? if <1.20
        //return state.canBeReplaced(Fluids.EMPTY)
    }

}
