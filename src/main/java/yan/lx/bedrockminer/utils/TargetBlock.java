package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.world.ClientWorld;
//import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
//import net.minecraft.world.World;

public class TargetBlock {
    private BlockPos blockPos;  // 目标方块位置
    private BlockPos redstoneTorchBlockPos; // 红石火把位置
    private BlockPos pistonBlockPos;  // 活塞位置
    private BlockPos slimeBlockPos; // 粘液块位置
    private ClientWorld world;  // 目标方块世界
    private Status status;  // 状态

    private int tickTimes;  // 玩家交互触发时,该任务目标方块处理的Tick计数器
    private boolean hasTried;   // 是否尝试过
    private int stuckTicksCounter;  // 卡住 Tick 计数器
    private int recycledTicksCounter;  // 卡住 Tick 计数器

    public TargetBlock(BlockPos pos, ClientWorld world) {
        this.hasTried = false;
        this.stuckTicksCounter = 0;
        this.recycledTicksCounter = 0;
        this.status = Status.Init;
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
                this.status = Status.Fail;
            }
        }
    }

    public Status tick() {
        updateStatus();
        // 处理刻时间超过最大值
        if (tickTimes > 40) {
            return Status.Fail;
        }
        return handler();
    }
    public Status handler() {
        tickTimes++;   // 该任务处理计数器
        switch (status) {
            // 未初始化
            case Init: {
                // 切换物品栏到活塞并放置
                InventoryManager.switchToItem(Blocks.PISTON);
                BlockPlacer.pistonPlacement(this.pistonBlockPos, Direction.UP);
                // 切换物品栏到红石火把并放置
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                break;
            }
            // 扩展(破坏活塞/红石火把,放置向下活塞)
            case EXTENDED: {
                // 打掉活塞附近的红石火把
                ArrayList<BlockPos> nearByRedstoneTorchPosList = CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos);
                for (BlockPos pos : nearByRedstoneTorchPosList) {
                    BlockBreaker.breakBlock(world, pos);
                }
                // 打掉活塞
                BlockBreaker.breakBlock(world, pistonBlockPos);
                // 放置朝下的活塞
                BlockPlacer.pistonPlacement(pistonBlockPos, Direction.DOWN);
                hasTried = true;
                break;
            }
            // 未扩展,未充能(放置红石火把)
            case NoExtendedPowerSource: {
                // 切换物品栏到红石火把并放置
                InventoryManager.switchToItem(Blocks.REDSTONE_TORCH);
                BlockPlacer.simpleBlockPlacement(this.redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                break;
            }
            // 卡住了(可能目标破坏失败了。破坏活塞让更新状态重新尝试)
            case STUCK: {
                // 破坏活塞
                if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON)) {
                    BlockBreaker.breakBlock(world, pistonBlockPos);
                }
                if (world.getBlockState(pistonBlockPos.up()).isOf(Blocks.PISTON)) {
                    BlockBreaker.breakBlock(world, pistonBlockPos.up());
                }
                if (world.getBlockState(pistonBlockPos.up().up()).isOf(Blocks.PISTON)) {
                    BlockBreaker.breakBlock(world, pistonBlockPos.up().up());
                }
                break;
            }
            // 回收物品
            case RecycledItems: {
                // 延迟1tick回收
                if (recycledTicksCounter > 1) {
                    // 破坏活塞
                    if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON)) {
                        BlockBreaker.breakBlock(world, pistonBlockPos);
                    }
                    if (world.getBlockState(pistonBlockPos.up()).isOf(Blocks.PISTON)) {
                        BlockBreaker.breakBlock(world, pistonBlockPos.up());
                    }
                    if (world.getBlockState(pistonBlockPos.up().up()).isOf(Blocks.PISTON)) {
                        BlockBreaker.breakBlock(world, pistonBlockPos.up().up());
                    }
                    if (redstoneTorchBlockPos != null && !world.getBlockState(redstoneTorchBlockPos).isAir()) {
                        BlockBreaker.breakBlock(world, redstoneTorchBlockPos);
                    }
                    if (slimeBlockPos != null && !world.getBlockState(slimeBlockPos).isAir()) {
                        BlockBreaker.breakBlock(world, slimeBlockPos);
                    }
                    return Status.Finish;
                }
                recycledTicksCounter++;
            }
            // 失败
            case Fail: {
                return Status.RecycledItems; // 失败设置回收物品状态
            }
            // 卡住(破坏失败状态)
            case NeedWait: {
                stuckTicksCounter++;
                break;
            }
            case MovingPiston: {
                break;
            }
        }
        return null;
    }

    /*** 更新状态 ***/
    private void updateStatus() {
        // 检查一下目标方块是否还有必要继续处理
        Block block = null;
        for (Block tmp : BreakingFlowController.allowBreakBlockList) {
            if (world.getBlockState(blockPos).isOf(tmp)) {
                block = tmp;
                break;
            }
        }

        // 目标方块已不存在,直接返回
        if (block == null) {
            status = Status.RecycledItems;
            return;
        }

        // 查找目标方块可放置红石火把位置
        redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(world, blockPos);
        if (redstoneTorchBlockPos == null) {
            // 查找粘液块位置
            slimeBlockPos = CheckingEnvironment.findPossibleSlimeBlockPos(world, blockPos);
            if (slimeBlockPos != null) {
                // 放置粘液块
                BlockPlacer.simpleBlockPlacement(slimeBlockPos, Blocks.SLIME_BLOCK);
                // 设置当前红石火把放置的位置
                redstoneTorchBlockPos = slimeBlockPos.up();
            }
            // 没有条件放置红石火把
            else {
                status = Status.Fail;
                Messager.actionBar("bedrockminer.fail.place.redstonetorch");
            }
        }
        // 活塞已放置,活塞已充能
        else if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON) && world.getBlockState(pistonBlockPos).get(PistonBlock.EXTENDED)) {
            status = Status.EXTENDED;  // 设置扩展状态,执行下一步任务
        }
        // 移动的活塞(活塞推拉方块属于技术性方块,一般称作36号方块)
        else if (world.getBlockState(pistonBlockPos).isOf(Blocks.MOVING_PISTON) || world.getBlockState(pistonBlockPos.up()).isOf(Blocks.MOVING_PISTON)) {
            status = Status.MovingPiston;
        }
        // 扩展执行过,活塞存在,卡住刻计数器 < 20 (等待状态更新下一个状态)
        else if (hasTried && world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && stuckTicksCounter < 15) {
            status = Status.NeedWait;
        }
        // 活塞存在,但是朝向向下,未充能,附近有红石火把,目标方块还在 (可能失败了,重置状态重新尝试)
        else if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON) && world.getBlockState(pistonBlockPos).get(PistonBlock.FACING) == Direction.DOWN && !world.getBlockState(this.pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos).size() != 0 && world.getBlockState(blockPos).isOf(block)) {
            status = Status.STUCK;
            hasTried = false;
            stuckTicksCounter = 0;
        }
        // 活塞存在,但是朝向向上,未充能,附近没有红石火把,目标方块还在 (还未执行,但活塞没有充能了,可能被其他任务执行的时候打掉了)
        else if (this.world.getBlockState(this.pistonBlockPos).isOf(Blocks.PISTON) && !world.getBlockState(pistonBlockPos).get(PistonBlock.EXTENDED) && this.world.getBlockState(this.pistonBlockPos).get(PistonBlock.FACING) == Direction.UP && CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos).size() == 0 && world.getBlockState(blockPos).isOf(block)) {
            this.status = Status.NoExtendedPowerSource;
        }
        // 活塞存在,活塞未充能,附近有可放置红石火把的位置,目标方块还在
        else if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON) && !this.world.getBlockState(pistonBlockPos).get(PistonBlock.EXTENDED) && CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos).size() != 0 && world.getBlockState(blockPos).isOf(block)) {
            status = Status.NoExtendedPowerSource;
        }
        // 有2方块放置活塞的地方
        else if (CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, blockPos)) {
            this.status = Status.Init;
        }
        // 检查上方是否放置活塞
        else if (!CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, blockPos)) {
            this.status = Status.Fail;
            Messager.actionBar("bedrockminer.fail.place.piston");
        } else {
            this.status = Status.Fail;
        }


    }


    /*** 状态类型 ***/
    public enum Status {
        /*** 初始化状态 ***/
        Init,

        /*** 扩展状态(活塞打掉放置相反活塞过程) ***/
        EXTENDED,

        /*** 无扩展电源状态(红石火把可能被其他任务方块运行时被打掉,所以要判断这种情况) ***/
        NoExtendedPowerSource,

        /*** 活塞 ***/
        MovingPiston,

        /*** 回收物品 ***/
        RecycledItems,

        /*** 需要等待 ***/
        NeedWait,

        /*** 卡住 ***/
        STUCK,

        /*** 失败 ***/
        Fail,

        /*** 完成 ***/
        Finish
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
}
