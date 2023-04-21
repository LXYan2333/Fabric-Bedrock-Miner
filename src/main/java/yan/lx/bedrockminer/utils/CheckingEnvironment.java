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
        if ((sideCoversSmallSquare(world, blockPos.east(), Direction.UP) && (world.getBlockState(blockPos.east().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.east().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.east();
        } else if ((sideCoversSmallSquare(world, blockPos.west(), Direction.UP) && (world.getBlockState(blockPos.west().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.west().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.west();
        } else if ((sideCoversSmallSquare(world, blockPos.north(), Direction.UP) && (world.getBlockState(blockPos.north().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.north().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.north();
        } else if ((sideCoversSmallSquare(world, blockPos.south(), Direction.UP) && (world.getBlockState(blockPos.south().up()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.south().up()).isOf(Blocks.REDSTONE_TORCH))) {
            return blockPos.south();
        }
        return null;
    }

    public static BlockPos findPossibleSlimeBlockPos(ClientWorld world, BlockPos blockPos) {
        for (Direction direction :Direction.Type.HORIZONTAL){
            BlockPos newBlockPos = blockPos.offset(direction);
            if (!world.getBlockState(newBlockPos).getMaterial().isReplaceable()){
                continue;
            }
            if (CheckingEnvironment.isBlocked(newBlockPos)){
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
        return world.getBlockState(blockPos.up().up()).getMaterial().isReplaceable();
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

    public static boolean isBlocked(BlockPos blockPos){
        ItemPlacementContext context = new ItemPlacementContext(MinecraftClient.getInstance().player,
                Hand.MAIN_HAND,
                Blocks.SLIME_BLOCK.asItem().getDefaultStack(),
                new BlockHitResult(blockPos.toCenterPos(),Direction.UP,blockPos,false));
        return !Blocks.SLIME_BLOCK.asItem().useOnBlock(context).isAccepted();
    }
}
