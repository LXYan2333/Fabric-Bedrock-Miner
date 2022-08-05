package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
//import net.minecraft.block.RedstoneTorchBlock;
//import net.minecraft.util.math.Direction;

//import java.util.ArrayList;

//import static net.minecraft.block.Block.sideCoversSmallSquare;

public class BlockBreaker {
    public static ArrayList<Block> blocksOfInterest = new ArrayList<>(Arrays.stream(new Block[]{Blocks.BEDROCK, Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL, Blocks.OBSIDIAN}).toList());

    public static void breakBlock(ClientWorld world, BlockPos pos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
        MinecraftClient.getInstance().interactionManager.attackBlock(pos, Direction.UP);
    }


}