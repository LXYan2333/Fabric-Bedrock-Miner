package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.compat.MinecraftClientCompat
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object BlockPlacer {

    fun simpleBlockPlacement(pos: BlockPos, item: Item) {
        InteractionRangeChecker.checkRange(pos)
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        val hitResult = BlockHitResult(Vec3.atBottomCenterOf(pos), Direction.DOWN, pos, false)
        //? if >= 1.21.1 {
        if (!InventoryManager.switchItemToOffHand(item)) return
        MinecraftClientCompat.useItemOn(player, InteractionHand.OFF_HAND, hitResult)
        //?} else {
        /*if (!InventoryManager.switchToItem(item)) return
        MinecraftClientCompat.useItemOn(player, InteractionHand.MAIN_HAND, hitResult)
        *///?}
    }

    fun getYawPitch(direction: Direction): Pair<Float, Float> {

        val pitch = when (direction) {
            Direction.UP -> 90f
            Direction.DOWN -> -90f
            else -> 0f
        }

        val yaw = when (direction) {
            Direction.SOUTH -> 180f
            Direction.EAST -> 90f
            Direction.NORTH -> 0f
            Direction.WEST -> -90f
            else -> 0f
        }
        return Pair(yaw, pitch)
    }

    fun vanillaPistonPlacement1(direction: Direction): Pair<Float, Float> {
        val client = Minecraft.getInstance()
        val player = client.player ?: return Pair(0.0F, 0.0F)

        val ret = getYawPitch(direction)

        client.connection?.send(MinecraftClientCompat.rotationPacket(ret.first, ret.second, MinecraftClientCompat.isOnGround(player)))

        return ret
    }

    fun vanillaPistonPlacement2(pos: BlockPos, direction: Direction) {
        InteractionRangeChecker.checkRange(pos)
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        //? if >= 1.21.1 {
        if (!InventoryManager.switchItemToOffHand(Blocks.PISTON.asItem())) return
        //?} else
        //if (!InventoryManager.switchToItem(Blocks.PISTON.asItem())) return

        val hitPos = pos.relative(direction.opposite)
        val hitVec = MinecraftClientCompat.offset(Vec3.atCenterOf(hitPos), direction, 0.5)
        val hitResult = BlockHitResult(hitVec, direction, pos, false)

        // these two varialbe is the actually used value when determine the piston facing.
        // we set them on client so the piston facing is correct on client.
        val oldXRot = player.xRot
        val oldYRot = player.yRot
        val oldYHeadRot = player.yHeadRot

        val (yaw, pitch) = getYawPitch(direction)
        try {
            player.xRot = pitch
            player.yRot = yaw
            player.yHeadRot = yaw
            client.connection?.send(MinecraftClientCompat.rotationPacket(yaw, pitch, MinecraftClientCompat.isOnGround(player)))
            //? if >= 1.21.1 {
            MinecraftClientCompat.useItemOn(player, InteractionHand.OFF_HAND, hitResult)
            //?} else
            //MinecraftClientCompat.useItemOn(player, InteractionHand.MAIN_HAND, hitResult)
        } finally {
            player.xRot = oldXRot
            player.yRot = oldYRot
            player.yHeadRot = oldYHeadRot
        }
    }

    fun vanillaPistonPlacement(pos: BlockPos, direction: Direction) {
        vanillaPistonPlacement1(direction)
        vanillaPistonPlacement2(pos, direction)
    }

    /**
     * Carpet Extra accurate block placement: encodes facing direction in the
     * hit X coordinate as {@code pos.x + 2 + direction.ordinal * 2}.
     * The server-side Carpet Extra mixin decodes this to override facing.
     */
    fun carpetPistonPlacement(pos: BlockPos, direction: Direction) {
        InteractionRangeChecker.checkRange(pos)
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        //? if >= 1.21.1 {
        if (!InventoryManager.switchItemToOffHand(Blocks.PISTON.asItem())) return
        //?} else
        //if (!InventoryManager.switchToItem(Blocks.PISTON.asItem())) return
        val hitVec = Vec3(pos.x + 2 + direction.ordinal * 2.0, pos.y.toDouble(), pos.z.toDouble())
        val hitResult = BlockHitResult(hitVec, direction, pos, false)

        val oldXRot = player.xRot
        val oldYRot = player.yRot
        val oldYHeadRot = player.yHeadRot
        val (yaw, pitch) = getYawPitch(direction)

        try {
            player.xRot = pitch
            player.yRot = yaw
            player.yHeadRot = yaw
            //? if >= 1.21.1 {
            MinecraftClientCompat.useItemOn(player, InteractionHand.OFF_HAND, hitResult)
            //?} else
            //MinecraftClientCompat.useItemOn(player, InteractionHand.MAIN_HAND, hitResult)
        } finally {
            player.xRot = oldXRot
            player.yRot = oldYRot
            player.yHeadRot = oldYHeadRot
        }
    }
}
