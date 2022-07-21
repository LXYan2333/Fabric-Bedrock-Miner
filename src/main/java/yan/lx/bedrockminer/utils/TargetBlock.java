package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.world.ClientWorld;
//import net.minecraft.item.ItemStack;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.logging.Logger;

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
        switch (this.status) {
            // 未初始化
            case UNINITIALIZED: {
                // 放置活塞
                InventoryManager.switchToItem(Blocks.PISTON);
                BlockPlacer.pistonPlacement(this.pistonBlockPos, Direction.UP);
                // 放置红石火把
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                return Status.UNINITIALIZED;
            }
            case UNEXTENDED_WITH_POWER_SOURCE:
                return Status.UNEXTENDED_WITH_POWER_SOURCE;
            // 扩展(活塞收回瞬间放置相反活塞)
            case EXTENDED: {
                // 打掉附近的红石火把
                ArrayList<BlockPos> nearByRedstoneTorchPosList = CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos);
                for (BlockPos pos : nearByRedstoneTorchPosList) {
                    BlockBreaker.breakBlock(world, pos);
                }
                // 打掉活塞
                BlockBreaker.breakBlock(this.world, this.pistonBlockPos);
                // 放置朝下的活塞
                BlockPlacer.pistonPlacement(this.pistonBlockPos, Direction.DOWN);
                this.hasTried = true;
                return Status.EXTENDED;
            }
            // 收回物品
            case RETRACTED: {
                BlockBreaker.breakBlock(world, pistonBlockPos);
                BlockBreaker.breakBlock(world, pistonBlockPos.up());
                if (this.slimeBlockPos != null) {
                    BlockBreaker.breakBlock(world, slimeBlockPos);
                }
                return Status.RETRACTED;
            }
            case RETRACTING:
                return Status.RETRACTING;
            case UNEXTENDED_WITHOUT_POWER_SOURCE: {
                // 放置红石火把
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                return Status.UNEXTENDED_WITHOUT_POWER_SOURCE;
            }
            // 失败
            case FAILED: {
                // 失败后回收物品
                recycleItems();
                return Status.FAILED;
            }
            // 卡住
            case STUCK:
                recycleItems();
                return Status.STUCK;
            case NEEDS_WAITING:
                return Status.NEEDS_WAITING;
            case Finish: {
                recycleItems();
                return Status.Finish;
            }
        }
        return null;
    }

    private void recycleItems() {
        // 成功后回收物品
        BlockBreaker.breakBlock(world, blockPos);
        BlockBreaker.breakBlock(world, blockPos.up());
        if (this.slimeBlockPos != null) {
            BlockBreaker.breakBlock(world, slimeBlockPos);
        }
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
        STUCK,
        Finish;
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
        // 处理刻时间超过最大值
        if (tickTimes > 40) {
            this.status = Status.FAILED;
            return;
        }
        // 检查一下目标方块是否还有必要继续处理
        Block block = null;
        for (Block tmp : BreakingFlowController.allowBreakBlockList) {
            if (world.getBlockState(this.blockPos).isOf(tmp)) {
                block = tmp;
                break;
            }
        }
        // 目标方块已不存在,直接返回完成状态
        if (block == null) {
            // 活塞在移动中
            if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.MOVING_PISTON)) {
                return;
            }
            // 活塞存在,但是朝向向下(说明已执行过),未充能,附近有红石火把,目标方块还在
            if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.DOWN) {
                this.status = Status.Finish;
            }
            return;
        }
        // 查找红石火把位置
        this.redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(this.world, this.blockPos);
        if (this.redstoneTorchBlockPos == null) {
            // 查找粘液块位置
            this.slimeBlockPos = CheckingEnvironment.findPossibleSlimeBlockPos(world, blockPos);
            if (slimeBlockPos != null) {
                // 放置粘液块
                BlockPlacer.simpleBlockPlacement(slimeBlockPos, Blocks.SLIME_BLOCK);
                // 获取红石火把位置
                redstoneTorchBlockPos = slimeBlockPos.up();
            }
            // 没有条件放置红石火把
            else {
                this.status = Status.FAILED;
                Messager.actionBar("bedrockminer.fail.place.redstonetorch");
            }
        }

        // 扩展(活塞放置成功,且已充能,准备执行破方块)
        else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED)) {
            this.status = Status.EXTENDED;
        }
        // 活塞移动中
        else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.MOVING_PISTON)) {
            this.status = Status.RETRACTING;
        }
        // 活塞存在且附近有红石火把充能,且目标方块还在(这种情况一般是未执行)
        else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() != 0 && this.world.getBlockState(this.blockPos).isOf(block)) {
            this.status = Status.UNEXTENDED_WITH_POWER_SOURCE;
        }
        // 执行过,活塞存在,卡住刻计数器
        else if (this.hasTried && this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.stuckTicksCounter < 20) {
            this.status = Status.NEEDS_WAITING;
            this.stuckTicksCounter++;
        }
        // 活塞存在,但是朝向向下(说明已执行过),未充能,附近有红石火把,目标方块还在
        else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.DOWN && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() != 0 && this.world.getBlockState(this.blockPos).isOf(block)) {
            this.status = Status.STUCK;
            this.hasTried = false;
            this.stuckTicksCounter = 0;
        }
        // 活塞存在,但是朝向向上(说明未执行过),未充能,附近没有红石火把,目标方块还在
        else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.UP && CheckingEnvironment.findNearbyRedstoneTorch(this.world, this.pistonBlockPos).size() == 0 && this.world.getBlockState(this.blockPos).isOf(block)) {
            this.status = Status.UNEXTENDED_WITHOUT_POWER_SOURCE;
        }
        // 有2方块放置活塞的地方
        else if (CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, this.blockPos)) {
            this.status = Status.UNINITIALIZED;
        }
        // 没有2方块放置活塞的地方
        else if (!CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, this.blockPos)) {
            this.status = Status.FAILED;
            Messager.actionBar("bedrockminer.fail.place.piston");
        } else {
            this.status = Status.FAILED;
        }


    }


}
