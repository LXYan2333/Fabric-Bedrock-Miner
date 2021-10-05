package yan.lx.bedrockminer.utils;

import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.world.ClientWorld;
//import net.minecraft.item.ItemStack;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

import static net.minecraft.block.Block.sideCoversSmallSquare;
//import net.minecraft.world.World;

public class TargetBlock {
    private BlockPos blockPos;
    private BlockPos redstoneTorchBlockPos;
    private BlockPos pistonBlockPos;
    private ClientWorld world;
    private Status status;
    private BlockPos slimeBlockPos;
    private int tickTimes;
    private boolean hasTried;
    private int stuckTicksCounter;

    public TargetBlock(BlockPos pos, ClientWorld world) {
        this.hasTried = false;
        this.stuckTicksCounter = 0;
        this.status = Status.UNINITIALIZED;
        this.blockPos = pos;
        this.world = world;
        this.pistonBlockPos = pos.up();
        this.redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(this.world, this.blockPos);
        if (redstoneTorchBlockPos == null) {
            this.slimeBlockPos = CheckingEnvironment.findPossibleSlimeBlockPos(world, pos);
            if (slimeBlockPos != null) {
                BlockPlacer.simpleBlockPlacement(slimeBlockPos, Blocks.SLIME_BLOCK);
                redstoneTorchBlockPos = slimeBlockPos.up();
            } else {
                this.status = Status.FAILED;
            }
        }
    }

    public Status tick() {
        this.tickTimes++;
        updateStatus();
        System.out.println(this.status);
        switch (this.status) {
            case UNINITIALIZED:
                InventoryManager.switchToItem(Blocks.PISTON);
                BlockPlacer.pistonPlacement(this.pistonBlockPos, Direction.UP);
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                break;
            case UNEXTENDED_WITH_POWER_SOURCE:
                break;
            case EXTENDED:
                //打掉红石火把
                ArrayList<BlockPos> nearByRedstoneTorchPosList = CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos);
                for (BlockPos pos : nearByRedstoneTorchPosList) {
                    BlockBreaker.breakBlock(world, pos);
                }
                //打掉活塞
                BlockBreaker.breakBlock(this.world, this.pistonBlockPos);
                //放置朝下的活塞
                BlockPlacer.pistonPlacement(this.pistonBlockPos, Direction.DOWN);
                this.hasTried = true;
                break;
            case RETRACTED:
                BlockBreaker.breakBlock(world, pistonBlockPos);
                BlockBreaker.breakBlock(world, pistonBlockPos.up());
                if (this.slimeBlockPos != null) {
                    BlockBreaker.breakBlock(world, slimeBlockPos);
                }
                return Status.RETRACTED;
            case RETRACTING:
                return Status.RETRACTING;
            case UNEXTENDED_WITHOUT_POWER_SOURCE:
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                break;
            case FAILED:
                BlockBreaker.breakBlock(world, pistonBlockPos);
                BlockBreaker.breakBlock(world, pistonBlockPos.up());
                return Status.FAILED;
            case STUCK:
                BlockBreaker.breakBlock(world, pistonBlockPos);
                BlockBreaker.breakBlock(world, pistonBlockPos.up());
                break;
            case NEEDS_WAITING:
                break;
        }
        return null;
    }

    enum Status {
        FAILED,
        UNINITIALIZED,
        UNEXTENDED_WITH_POWER_SOURCE,
        UNEXTENDED_WITHOUT_POWER_SOURCE,
        EXTENDED,
        NEEDS_WAITING,
        RETRACTING,
        RETRACTED,
        STUCK;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ClientWorld getWorld() {
        return world;
    }

    public Status getStatus() {
        return status;
    }

    private void updateStatus() {
        if (this.tickTimes > 40) {
            this.status = Status.FAILED;
            return;
        }
        this.redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(this.world, this.blockPos);
        if (this.redstoneTorchBlockPos == null) {
            this.status = Status.FAILED;
            Messager.actionBar("bedrockminer.fail.place.redstonetorch");
        } else if (!this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK) && this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON)) {
            this.status = Status.RETRACTED;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED)) {
            this.status = Status.EXTENDED;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.MOVING_PISTON)) {
            this.status = Status.RETRACTING;
        }  else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() != 0 && this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK)) {
            this.status = Status.UNEXTENDED_WITH_POWER_SOURCE;
        } else if (this.hasTried && this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.stuckTicksCounter < 15) {
            this.status = Status.NEEDS_WAITING;
            this.stuckTicksCounter++;
        } else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.DOWN && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() != 0 && this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK)) {
            this.status = Status.STUCK;
            this.hasTried = false;
            this.stuckTicksCounter = 0;
        }else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.UP && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() == 0 && this.world.getBlockState(this.blockPos).isOf(Blocks.BEDROCK)) {
            this.status = Status.UNEXTENDED_WITHOUT_POWER_SOURCE;
        } else if (CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, this.blockPos)) {
            this.status = Status.UNINITIALIZED;
        } else if (!CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, this.blockPos)) {
            this.status = Status.FAILED;
            Messager.actionBar("bedrockminer.fail.place.piston");
        } else {
            this.status = Status.FAILED;
        }
    }

}
