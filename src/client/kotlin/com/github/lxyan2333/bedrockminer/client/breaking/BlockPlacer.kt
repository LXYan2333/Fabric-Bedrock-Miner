package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object BlockPlacer {

    fun simpleBlockPlacement(pos: BlockPos, item: Item) {
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        if (!InventoryManager.switchToItem(item)) return
        val hitResult = BlockHitResult(Vec3.atBottomCenterOf(pos), Direction.DOWN, pos, false)
        client.gameMode?.useItemOn(player, InteractionHand.MAIN_HAND, hitResult)
    }

    fun pistonPlacement(pos: BlockPos, direction: Direction) {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        // Send look packet to trick server about placement direction
        val yaw = when (direction) {
            Direction.SOUTH -> 180f
            Direction.EAST -> 90f
            Direction.NORTH -> 0f
            Direction.WEST -> -90f
            else -> player.yRot
        }
        val pitch = when (direction) {
            Direction.UP -> 90f
            Direction.DOWN -> -90f
            else -> 0f
        }
        client.connection?.send(ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(), false))

        if (!InventoryManager.switchToItem(Blocks.PISTON.asItem())) return
        val hitPos = pos.relative(direction.opposite)
        val hitVec = Vec3.atCenterOf(hitPos).relative(direction, 0.5)
        val hitResult = BlockHitResult(hitVec, direction, pos, false)
        client.gameMode?.useItemOn(player, InteractionHand.MAIN_HAND, hitResult)
    }
}