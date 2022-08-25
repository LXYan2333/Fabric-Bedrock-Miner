package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TargetBlock {


    // 构造器传递的参数
    private BlockPos blockPos;
    private ClientWorld world;
    private final List<TargetBlock> cachedTargetBlockList;
    // 需要初始化的
    private Block block;
    @Nullable
    private BlockPos pistonBlockPos;
    @Nullable
    private BlockPos redstoneTorchBlockPos;
    @Nullable
    private BlockPos slimeBlockPos;
    private int tickTimes;
    private boolean hasTried;
    private int tickRecycleTimes;
    private int retryMax;
    private Status status;


    /**
     * 构造函数
     *
     * @param cachedTargetBlockList 缓存目标任务列表
     * @param blockPos              目标方块所在位置
     * @param world                 目标方块所在世界
     */
    public TargetBlock(BlockPos blockPos, ClientWorld world, List<TargetBlock> cachedTargetBlockList) {
        // 赋值
        this.blockPos = blockPos;
        this.world = world;
        this.cachedTargetBlockList = cachedTargetBlockList;
        // 初始化
        this.block = world.getBlockState(this.blockPos).getBlock();
        this.pistonBlockPos = null;
        this.slimeBlockPos = null;
        this.redstoneTorchBlockPos = null;
        this.tickTimes = 0;
        this.tickRecycleTimes = 0;
        this.retryMax = 1;
        this.hasTried = false;
        this.status = Status.INITIALIZATION;
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


    /**
     * 更新处理程序
     */
    public void updater() {
        Debug.info("[" + tickTimes + "]" + "当前处理状态: " + status);
        if (!(status == Status.INITIALIZATION || status == Status.EXTENDED_START || status == Status.TIME_OUT
                || status == Status.FAILED || status == Status.ITEM_RECYCLING || status == Status.FINISH)) {
            // 处理超时判断
            if (tickTimes > 40) {
                Debug.info("[玩家交互更新]: 超时");
                status = Status.TIME_OUT;
                return; // 需要直接返回,否则进入状态更新,更新新的状态之后就进入了死循环了
            }
            // 更新当前状态
            updateStatus();
        }
        // 状态处理
        updateHandler();
        ++tickTimes;
    }


    private void updateHandler() {
        switch (status) {
            case INITIALIZATION -> {
                Debug.info("[初始化]: 准备");
                this.pistonBlockPos = null;
                this.slimeBlockPos = null;
                this.redstoneTorchBlockPos = null;
                this.tickTimes = 0;
                this.tickRecycleTimes = 0;
                this.hasTried = false;
                status = Status.EXTENDED_READY;
                Debug.info("[初始化]: 完成");
            }
            case FIND_PISTON_POSITION -> {
                pistonBlockPos = blockPos.up();
                // 检查活塞能否放置
                if (!CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, pistonBlockPos)) {
                    Messager.actionBar("bedrockminer.fail.place.piston");
                    status = Status.FAILED;
                    updateHandler();
                    return;
                }
                Debug.info("[查找活塞]: " + pistonBlockPos);
                status = Status.EXTENDED_READY;
            }
            case FIND_REDSTONE_TORCH -> {
                redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(world, blockPos);
                if (redstoneTorchBlockPos == null) {
                    slimeBlockPos = CheckingEnvironment.findPossibleSlimeBlockPos(world, blockPos);
                    Debug.info("[查找粘液块]: " + slimeBlockPos);
                    if (slimeBlockPos == null) {
                        Messager.actionBar("bedrockminer.fail.place.redstonetorch"); // 无法放置红石火把(没有可放置的基座方块)
                        status = Status.FAILED; // 无位置可以放置粘液块
                        updateHandler();
                        return;
                    }
                    redstoneTorchBlockPos = slimeBlockPos.up();
                }
                Debug.info("[查找红石火把]: " + redstoneTorchBlockPos);
                status = Status.EXTENDED_READY;
            }
            case PLACE_PISTON -> {
                Debug.info("[放置活塞]: 放置准备");
                InventoryManager.switchToItem(Blocks.PISTON);
                assert pistonBlockPos != null;
                BlockPlacer.pistonPlacement(pistonBlockPos, Direction.UP);
                Debug.info("[放置活塞]: 放置完成");
                status = Status.EXTENDED_READY;
            }
            case PLACE_SLIME_BLOCK -> {
                Debug.info("[放置粘液块]: 放置准备");
                BlockPlacer.simpleBlockPlacement(slimeBlockPos, Blocks.SLIME_BLOCK);
                Debug.info("[放置粘液块]: 放置完成");
                status = Status.EXTENDED_READY;
            }
            case PLACE_REDSTONE_TORCH -> {
                if (slimeBlockPos != null && !world.getBlockState(slimeBlockPos).isOf(Blocks.SLIME_BLOCK)) {
                    Debug.info("[放置红石火把]: 需要放置粘液块");
                    status = Status.PLACE_SLIME_BLOCK;
                    updateHandler();
                    return;
                }
                Debug.info("[放置红石火把]: 准备放置");
                BlockPlacer.simpleBlockPlacement(redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);
                Debug.info("[放置红石火把]: 放置完成");
                status = Status.EXTENDED_READY;
            }
            case EXTENDED_START -> {
                if (!hasTried) {
                    Debug.info("[扩展]：准备开始");
                    // 打掉活塞附近能充能的红石火把
                    assert pistonBlockPos != null;
                    ArrayList<BlockPos> nearByRedstoneTorchPosList = CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos);
                    for (BlockPos pos : nearByRedstoneTorchPosList) {
                        Debug.info("[扩展]：打掉红石火把, " + pos);
                        BlockBreaker.breakBlock(world, pos);
                    }
                    // 打掉活塞
                    Debug.info("[扩展]：打掉活塞, " + pistonBlockPos);
                    BlockBreaker.breakBlock(world, pistonBlockPos);

                    // 放置朝下的活塞
                    Debug.info("[扩展]：放置朝下的活塞, " + pistonBlockPos);
                    BlockPlacer.pistonPlacement(pistonBlockPos, Direction.DOWN);

                    hasTried = true;
                    Debug.info("[扩展]：完成扩展");
                    status = Status.NEED_WAIT;
                }
            }
            case TIME_OUT -> {
                status = Status.ITEM_RECYCLING;
            }
            case FAILED -> {
                if (retryMax-- > 0) {
                    Debug.info("[失败]：准备重试");
                    status = Status.INITIALIZATION;
                } else {
                    status = Status.ITEM_RECYCLING;
                    Debug.info("[失败]：准备物品回收");
                }
            }
            case ITEM_RECYCLING -> {
                // 延迟 2tick 交互回收
                if (tickRecycleTimes > 0) {
                    if (redstoneTorchBlockPos != null && world.getBlockState(redstoneTorchBlockPos).isOf(Blocks.REDSTONE_TORCH)) {
                        Debug.info("[物品回收][" + tickRecycleTimes + "]: 红石火把");
                        BlockBreaker.breakBlock(world, redstoneTorchBlockPos);
                    }
                    BlockState pistonBlockState = world.getBlockState(pistonBlockPos);
                    if (!pistonBlockState.isOf(Blocks.MOVING_PISTON) && !pistonBlockState.isAir()) {
                        BlockBreaker.breakBlock(world, pistonBlockPos);
                        Debug.info("[物品回收][" + tickRecycleTimes + "]: 活塞");
                    }
                    if (tickRecycleTimes > 40 || world.getBlockState(pistonBlockPos).isAir()) {
                        if (slimeBlockPos != null && world.getBlockState(slimeBlockPos).isOf(Blocks.SLIME_BLOCK)) {
                            BlockBreaker.breakBlock(world, slimeBlockPos);
                            Debug.info("[物品回收][" + tickRecycleTimes + "]: 粘液块");
                        }
                        if (pistonBlockPos != null && world.getBlockState(pistonBlockPos.up()).isOf(Blocks.PISTON)) {
                            BlockBreaker.breakBlock(world, pistonBlockPos.up());
                            Debug.info("[物品回收][" + tickRecycleTimes + "]: 活塞up");
                        }
                        if (pistonBlockPos != null && world.getBlockState(pistonBlockPos.up().up()).isOf(Blocks.PISTON)) {
                            BlockBreaker.breakBlock(world, pistonBlockPos.up().up());
                            Debug.info("[物品回收][" + tickRecycleTimes + "]: 活塞upup");
                        }
                        status = Status.FINISH;
                        Debug.info("[物品回收][" + tickRecycleTimes + "]: 物品完成");
                        return;
                    }
                }
                ++tickRecycleTimes;
            }
            case FINISH -> {
                cachedTargetBlockList.remove(this); // 将该任务从缓存任务列表中移除
            }
        }

    }

    private void updateStatus() {
        if (status == Status.PLACE_SLIME_BLOCK) {
            return;
        }
        if (pistonBlockPos == null) {
            Debug.info("[更新状态]: 活塞坐标位置未获取");
            status = Status.FIND_PISTON_POSITION;
            updateHandler();
        }
        if (redstoneTorchBlockPos == null) {
            Debug.info("[更新状态]: 红石火把位置未获取");
            status = Status.FIND_REDSTONE_TORCH;
            updateHandler();
        }
        // 优先检查目标方块是否存在
        if (!world.getBlockState(blockPos).isOf(block)) {
            Debug.info("[更新状态]: 方块不存在");
            // 检查是否需要回收
            boolean isRecyclePistonBlock = pistonBlockPos != null && world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON);
            boolean isRecycleRedstoneTorchBlock = redstoneTorchBlockPos != null && world.getBlockState(redstoneTorchBlockPos).isOf(Blocks.REDSTONE_TORCH);
            boolean isRecycleSlimeBlock = slimeBlockPos != null && world.getBlockState(slimeBlockPos).isOf(Blocks.SLIME_BLOCK);
            if (isRecyclePistonBlock || isRecycleRedstoneTorchBlock || isRecycleSlimeBlock) {
                Debug.info("[更新状态]: 需要回收物品");
                status = Status.ITEM_RECYCLING;
                return;
            }
            // 没有物品回收则直接返回完成！
            Debug.info("[更新状态]: 任务完成");
            status = Status.FINISH;
            return;
        }

        // 检查活塞当前是否为技术性方块(36号方块)
        if (world.getBlockState(pistonBlockPos).isOf(Blocks.MOVING_PISTON)) {
            Debug.info("[更新状态]: 活塞移动中");
            status = Status.PISTON_MOVING;
            return;
        }
        // 未扩展过
        if (!hasTried) {
            // 活塞存在
            if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON)) {
                Debug.info("[更新状态]: 活塞存在");
                // 活塞已充能(扩展开始)
                if (world.getBlockState(pistonBlockPos).get(PistonBlock.EXTENDED)) {
                    Debug.info("[更新状态]: 条件充足,准备扩展");
                    status = Status.EXTENDED_START;
                }
                // 活塞未充能, 活塞朝向：向上(红石火把可能被之前的任务给清理掉了,需要重新放置)
                else {
                    if (world.getBlockState(pistonBlockPos).get(PistonBlock.FACING) == Direction.UP) {
                        Debug.info("[更新状态]: 需要放置红石火把");
                        status = Status.PLACE_REDSTONE_TORCH;
                    }
                }
            }
            // 活塞不存在
            else {
                Debug.info("[更新状态]: 活塞不存在");
                status = Status.PLACE_PISTON;
                updateHandler();
            }
        }
        // 已扩展过
        else {
            if (status != Status.NEED_WAIT) {
                Debug.info("[更新状态]: 失败了");
                status = Status.FAILED;
            }
        }

    }

}


