package yan.lx.bedrockminer.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;


public class BlockBreaker {
    public static void breakBlock(BlockPos pos) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerInteractionManager interactionManager = minecraftClient.interactionManager;
        if (interactionManager == null || pos == null) {
            return;
        }
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
        interactionManager.attackBlock(pos, Direction.UP);
    }
}