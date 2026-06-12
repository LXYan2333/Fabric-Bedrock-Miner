package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.client.Minecraft
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
import net.minecraft.world.inventory.ContainerInput
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
            inventory.selectedSlot = slot
        } else {
            pickFromInventory(slot)
        }
        client.connection?.send(ServerboundSetCarriedItemPacket(inventory.selectedSlot))
        return true
    }

    private fun pickFromInventory(slot: Int) {
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        val gameMode = client.gameMode ?: return
        val inventory = player.inventory

        val switch = inventory.suitableHotbarSlot
        gameMode.handleContainerInput(
            player.containerMenu.containerId, slot, switch,
            ContainerInput.SWAP, player
        )
        inventory.selectedSlot = switch
    }

    private fun findSlotWithItem(inventory: Inventory, item: Item): Int {
        for (i in 0 until inventory.containerSize) {
            if (inventory.getItem(i).`is`(item)) return i
        }
        return -1
    }

    private fun getEfficientToolSlot(inventory: Inventory): Int {
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
        for (stack in player.inventory) {
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
            val efficiency_level =
                Minecraft.getInstance().level!!.registryAccess().lookup(Registries.ENCHANTMENT).get().getOrThrow(
                    Enchantments.EFFICIENCY
                )

            val level = EnchantmentHelper.getItemEnchantmentLevel(efficiency_level, stack)
            if (level > 0 && !stack.isEmpty) {
                speed += (level * level + 1).toFloat()
            }
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            speed *= 1.0f + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2f
        }

        if (player.hasEffect(MobEffects.MINING_FATIGUE)) {
            val amplifier = player.getEffect(MobEffects.MINING_FATIGUE)?.amplifier ?: 0
            speed *= when (amplifier) {
                0 -> 0.3f
                1 -> 0.09f
                2 -> 0.0027f
                else -> 8.1E-4f
            }
        }

        speed *= player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED).toFloat();

		if (player.isEyeInFluid(FluidTags.WATER)) {
			speed *= player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED).toFloat();
		}

        if (!player.onGround()) {
            speed /= 5.0f
        }

        return speed
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
        if (countItem(Configs.Generic.SUPPORT_BLOCK.blockStateValue.block.asItem()) < 1) {
            val supportBlockName = Configs.Generic.SUPPORT_BLOCK.blockStateValue.block.name.string
            return StringUtils.translate("bedrockminer.message.need_support", supportBlockName)
        }
        if (!canInstantlyMinePiston()) {
            return StringUtils.translate("bedrockminer.message.need_efficiency")
        }
        return null
    }
}