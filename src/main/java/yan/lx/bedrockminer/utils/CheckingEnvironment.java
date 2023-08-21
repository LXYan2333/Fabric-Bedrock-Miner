package yan.lx.bedrockminer.utils;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

import static net.minecraft.block.Block.sideCoversSmallSquare;

public class CheckingEnvironment {

    public static BlockPos findNearbyFlatBlockToPlaceRedstoneTorch(ClientWorld world, BlockPos blockPos) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            Direction redstoneTorchFacing = Direction.UP;           // 红石火把的朝向
            BlockPos baseBlockPos = blockPos.offset(direction);     // 底座位置
            BlockPos redstoneTorchPos = baseBlockPos.offset(redstoneTorchFacing);   // 红石火把位置
            // 红石火把可以被放置情况下
            if (sideCoversSmallSquare(world, baseBlockPos, redstoneTorchFacing) && world.getBlockState(redstoneTorchPos).isReplaceable()) {
                return baseBlockPos;
            } else if (world.getBlockState(redstoneTorchPos).isOf(Blocks.REDSTONE_TORCH)) { // 红石火把已放置情况下
                return baseBlockPos;
            }
        }
        return null;
    }

    public static BlockPos findPossibleSlimeBlockPos(ClientWorld world, BlockPos blockPos) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos newBlockPos = blockPos.offset(direction);
            // 粘液块无法被替换情况下
            if (!world.getBlockState(newBlockPos).isReplaceable()) {
                continue;
            }
            // 粘液块放置是否会被阻止
            if (CheckingEnvironment.isBlocked(newBlockPos)) {
                continue;
            }
            return newBlockPos;
        }
        return null;
    }

    public static boolean has2BlocksOfPlaceToPlacePiston(ClientWorld world, BlockPos blockPos) {
        if (world.getBlockState(blockPos.up()).getHardness(world, blockPos.up()) == 0) {
            BlockBreaker.breakBlock(world, blockPos.up());
        }
        if (isBlocked(blockPos.up())) {
            return false;
        }
        return world.getBlockState(blockPos.up().up()).isReplaceable();
    }

    public static ArrayList<BlockPos> findNearbyRedstoneTorch(ClientWorld world, BlockPos pistonBlockPos) {
        ArrayList<BlockPos> list = new ArrayList<>();
        if (world.getBlockState(pistonBlockPos.east()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.east());
        }
        if (world.getBlockState(pistonBlockPos.west()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.west());
        }
        if (world.getBlockState(pistonBlockPos.south()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.south());
        }
        if (world.getBlockState(pistonBlockPos.north()).isOf(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.north());
        }
        return list;
    }

    public static boolean isBlocked(BlockPos blockPos) {
        ItemPlacementContext context = new ItemPlacementContext(MinecraftClient.getInstance().player,
                Hand.MAIN_HAND,
                Blocks.SLIME_BLOCK.asItem().getDefaultStack(),
                new BlockHitResult(blockPos.toCenterPos(), Direction.UP, blockPos, false));
        return !Blocks.SLIME_BLOCK.asItem().useOnBlock(context).isAccepted();
    }
}
