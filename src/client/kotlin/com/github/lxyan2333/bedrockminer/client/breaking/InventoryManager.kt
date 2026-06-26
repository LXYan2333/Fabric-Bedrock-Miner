package com.github.lxyan2333.bedrockminer.client.breaking

import com.github.lxyan2333.bedrockminer.client.compat.MinecraftClientCompat
import net.minecraft.client.Minecraft
//? if >=1.21
import net.minecraft.core.registries.Registries
import net.minecraft.tags.FluidTags
import net.minecraft.world.effect.MobEffectUtil
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
import net.minecraft.world.entity.ai.attributes.Attributes
import fi.dy.masa.malilib.util.StringUtils
import com.github.lxyan2333.bedrockminer.client.config.Configs

object InventoryManager {
    private const val INSTANT_MINE_THRESHOLD = 45f

    fun switchToItem(item: Item): Boolean {
        val client = Minecraft.getInstance()
        val player = client.player ?: return false
        val inventory = player.inventory

        var slot = findSlotWithItem(inventory, item)
        if (item == Items.DIAMOND_PICKAXE) {
            slot = getEfficientToolSlot(inventory)
        }

        if (slot == -1) return false

        if (Inventory.isHotbarSlot(slot)) {
            MinecraftClientCompat.setSelectedSlot(inventory, slot)
        } else {
            pickFromInventory(slot)
        }
        client.connection?.send(ServerboundSetCarriedItemPacket(MinecraftClientCompat.selectedSlot(inventory)))
        return true
    }

    private fun pickFromInventory(slot: Int) {
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        val inventory = player.inventory

        val switch = inventory.suitableHotbarSlot
        MinecraftClientCompat.swapInventorySlot(player.containerMenu.containerId, slot, switch, player)
        MinecraftClientCompat.setSelectedSlot(inventory, switch)
    }

    private fun findSlotWithItem(inventory: Inventory, item: Item): Int {
        for (i in 0 until inventory.containerSize) {
            if (MinecraftClientCompat.stackIs(inventory.getItem(i), item)) return i
        }
        return -1
    }

    private fun getEfficientToolSlot(inventory: Inventory): Int {
        if (Configs.Generic.SKIP_INSTANT_MINE_CHECK.booleanValue) {
            var bestSlot = -1
            var bestSpeed = 0f
            for (i in 0 until inventory.containerSize) {
                val speed = getBlockBreakingSpeed(Blocks.PISTON.defaultBlockState(), inventory.getItem(i))
                if (speed > bestSpeed) {
                    bestSpeed = speed
                    bestSlot = i
                }
            }
            return bestSlot
        }
        for (i in 0 until inventory.containerSize) {
            if (getBlockBreakingSpeed(
                    Blocks.PISTON.defaultBlockState(),
                    inventory.getItem(i)
                ) > INSTANT_MINE_THRESHOLD
            ) {
                return i
            }
        }
        return -1
    }

    fun canInstantlyMinePiston(): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val inventory = player.inventory
        for (i in 0 until inventory.containerSize) {
            val stack = inventory.getItem(i)
            if (stack.isEmpty) continue
            if (getBlockBreakingSpeed(Blocks.PISTON.defaultBlockState(), stack) > INSTANT_MINE_THRESHOLD) {
                return true
            }
        }
        return false
    }

    private fun getBlockBreakingSpeed(block: BlockState, stack: ItemStack): Float {
        val player = Minecraft.getInstance().player ?: return 0f

        var speed = stack.getDestroySpeed(block)
        if (speed > 1.0f) {
            //? if >=1.21 {
            val efficiency_level =
                Minecraft.getInstance().level!!.registryAccess().lookup(Registries.ENCHANTMENT).get().getOrThrow(
                    Enchantments.EFFICIENCY
                )

            val level = EnchantmentHelper.getItemEnchantmentLevel(efficiency_level, stack)
            //?} else if >=1.20.5 {
            //val level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.EFFICIENCY, stack)
            //?} else
            //val level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack)
            if (level > 0 && !stack.isEmpty) {
                speed += (level * level + 1).toFloat()
            }
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            speed *= 1.0f + (getDigSpeedAmplification() + 1) * 0.2f
        }

        //? if >=1.21.11 {
        val miningFatigue = MobEffects.MINING_FATIGUE
        //?} else
        //val miningFatigue = MobEffects.DIG_SLOWDOWN
        if (player.hasEffect(miningFatigue)) {
            val amplifier = player.getEffect(miningFatigue)?.amplifier ?: 0
            speed *= when (amplifier) {
                0 -> 0.3f
                1 -> 0.09f
                2 -> 0.0027f
                else -> 8.1E-4f
            }
        }

        //? if >=1.21 {
        speed *= player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED).toFloat()

		if (player.isEyeInFluid(FluidTags.WATER)) {
			speed *= player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED).toFloat()
		}
        //?} else {
        /*if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            speed /= 5.0f
        }
        *///?}

        if (!MinecraftClientCompat.isOnGround(player)) {
            speed /= 5.0f
        }

        return speed
    }

    private fun getDigSpeedAmplification(): Int {
        val player = Minecraft.getInstance().player ?: return 0
        //? if >=1.21 {
        return MobEffectUtil.getDigSpeedAmplification(player)
        //?} else {
        /*val haste = player.getEffect(MobEffects.DIG_SPEED)?.amplifier?.let(::unsignedByteAmplifier) ?: 0
        val conduit = player.getEffect(MobEffects.CONDUIT_POWER)?.amplifier?.let(::unsignedByteAmplifier) ?: 0
        return maxOf(haste, conduit)
        *///?}
    }

    private fun unsignedByteAmplifier(amplifier: Int): Int {
        return if (amplifier < 0) amplifier + 256 else amplifier
    }

    fun countItem(item: Item): Int {
        val player = Minecraft.getInstance().player ?: return 0
        return player.inventory.countItem(item)
    }

    fun checkRequiredItems(): String? {
        val client = Minecraft.getInstance()
        val gameMode = client.gameMode ?: return StringUtils.translate("bedrockminer.message.not_in_game")

        if (!gameMode.playerMode.isSurvival) {
            return StringUtils.translate("bedrockminer.message.survival_only")
        }
        if (countItem(Blocks.PISTON.asItem()) < 2) {
            return StringUtils.translate("bedrockminer.message.need_pistons")
        }
        if (countItem(Blocks.REDSTONE_TORCH.asItem()) < 1) {
            return StringUtils.translate("bedrockminer.message.need_torches")
        }
        val supportBlock = Configs.Generic.supportBlock
        if (countItem(supportBlock.asItem()) < 1) {
            val supportBlockName = supportBlock.name.string
            return StringUtils.translate("bedrockminer.message.need_support", supportBlockName)
        }
        if (!Configs.Generic.SKIP_INSTANT_MINE_CHECK.booleanValue && !canInstantlyMinePiston()) {
            return StringUtils.translate("bedrockminer.message.need_efficiency")
        }
        return null
    }
}
