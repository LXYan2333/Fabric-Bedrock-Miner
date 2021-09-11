package yan.lx.bedrockminer.utils;

//import net.fabricmc.fabric.api.event.client.player.ClientPickBlockCallback;
//import net.minecraft.client.MinecraftClient;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.tag.FluidTags;

public class InventoryManager {
    public static boolean switchToItem(ItemConvertible item) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerInventory playerInventory = minecraftClient.player.getInventory();

        int i = playerInventory.getSlotWithStack(new ItemStack(item));

        if ("diamond_pickaxe".equals(item.toString())) {
            i = getEfficientTool(playerInventory);
        }

        if (i != -1) {
            if (PlayerInventory.isValidHotbarIndex(i)) {
                playerInventory.selectedSlot = i;
            } else {
                minecraftClient.interactionManager.pickFromInventory(i);
            }
            minecraftClient.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(playerInventory.selectedSlot));
            return true;
        }
        return false;
    }

    private static int getEfficientTool(PlayerInventory playerInventory) {
        for (int i = 0; i < playerInventory.main.size(); ++i) {
            if (getBlockBreakingSpeed(Blocks.PISTON.getDefaultState(), i) > 45f) {
                return i;
            }
        }
        return -1;
    }

    public static boolean canInstantlyMinePiston() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerInventory playerInventory = minecraftClient.player.getInventory();

        for (int i = 0; i < playerInventory.size(); i++) {
            if (getBlockBreakingSpeed(Blocks.PISTON.getDefaultState(), i) > 45f) {
                return true;
            }
        }
        return false;
    }

    private static float getBlockBreakingSpeed(BlockState block, int slot) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;
        ItemStack stack = player.getInventory().getStack(slot);

        float f = stack.getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            ItemStack itemStack = player.getInventory().getStack(slot);
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(player)) {
            f *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2F;
        }

        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k;
            switch (player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    k = 0.3F;
                    break;
                case 1:
                    k = 0.09F;
                    break;
                case 2:
                    k = 0.0027F;
                    break;
                case 3:
                default:
                    k = 8.1E-4F;
            }

            f *= k;
        }

        if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.isOnGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public static int getInventoryItemCount(ItemConvertible item) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerInventory playerInventory = minecraftClient.player.getInventory();
        return playerInventory.count((Item) item);
    }

    public static String warningMessage() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (!"survival".equals(minecraftClient.interactionManager.getCurrentGameMode().getName())) {
            return "仅限生存模式！Survival Only!";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.PISTON) < 2) {
            return "活塞不够啦！ Needs more piston!";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.REDSTONE_TORCH) < 1) {
            return "红石火把不够啦！ Needs more redstone torch!";
        }

        if (InventoryManager.getInventoryItemCount(Blocks.SLIME_BLOCK)<1){
            return "黏液块不够啦！ Needs more slime block!";
        }

        if (!InventoryManager.canInstantlyMinePiston()) {
            return "无法秒破活塞！请确保效率Ⅴ+急迫Ⅱ Can't instantly mine piston! EfficiencyⅤ+HasteⅡ required!";
        }
        return null;
    }

}
