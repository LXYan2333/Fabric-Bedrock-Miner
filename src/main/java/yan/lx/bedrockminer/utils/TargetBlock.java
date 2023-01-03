package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import yan.lx.bedrockminer.Debug;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TargetBlock {
    private final Block block;
    private final BlockPos blockPos;
    private final ClientWorld world;

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

    private Queue<BlockPos> recyclingTask;

    /**
     * 构造函数
     *
     * @param block    目标方块
     * @param blockPos 目标方块所在位置
     * @param world    目标方块所在世界
     */
    public TargetBlock(Block block, BlockPos blockPos, ClientWorld world) {
        // 赋值
        this.block = block;
        this.blockPos = blockPos;
        this.world = world;
        // 初始化
        this.pistonBlockPos = null;
        this.slimeBlockPos = null;
        this.redstoneTorchBlockPos = null;
        this.tickTimes = 0;
        this.tickRecycleTimes = 0;
        this.retryMax = 1;
        this.hasTried = false;
        this.status = Status.INITIALIZATION;
        this.recyclingTask = new LinkedList<>();
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
    public boolean updater() {
        Debug.info(String.format("[%s]当前处理状态: %s", tickTimes, status));
        switch (this.status) {
            case TIME_OUT, FAILED, ITEM_RECYCLING, FINISH -> {
                return updateHandler();     // 状态处理
            }
            default -> {
                // 超时更新
                if (tickTimes++ > 40) {
                    Debug.info("[玩家交互更新]: 超时");
                    status = Status.TIME_OUT;
                    return false;
                }
                updateStatus();             // 更新当前状态
                return updateHandler();     // 状态处理
            }
        }
    }


    private boolean updateHandler() {
        switch (status) {
            case INITIALIZATION -> {
                Debug.info("[初始化]: 准备");
                this.tickTimes = 0;
                this.tickRecycleTimes = 0;
                this.hasTried = false;
                this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
                Debug.info("[初始化]: 完成");
            }
            case FIND_PISTON_POSITION -> {
                Debug.info("[查找活塞位置]: 准备");
                this.pistonBlockPos = blockPos.up();
                // 检查活塞能否放置
                if (CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, blockPos)) {
                    Debug.info("[查找活塞位置]: 完成, " + pistonBlockPos);
                    this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
                } else {
                    Messager.actionBar("bedrockminer.fail.place.piston");   // 无法放置活塞
                    this.status = Status.FAILED;  // 失败状态
                }
            }
            case FIND_REDSTONE_TORCH_POSITION -> {
                Debug.info("[查找红石火把位置]: 准备");
                this.redstoneTorchBlockPos = CheckingEnvironment.findNearbyFlatBlockToPlaceRedstoneTorch(world, blockPos);
                if (redstoneTorchBlockPos != null) {
                    Debug.info("[查找红石火把位置]: 成功, " + redstoneTorchBlockPos);
                    this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
                } else {
                    // 查找可以放置红石火把基座位置
                    Debug.info("[查找红石火把位置]: 失败, 准备尝试查找粘液块");
                    Debug.info("[查找粘液块位置]: 准备, " + this.slimeBlockPos);
                    this.slimeBlockPos = CheckingEnvironment.findPossibleSlimeBlockPos(world, blockPos);
                    if (this.slimeBlockPos != null) {
                        this.redstoneTorchBlockPos = this.slimeBlockPos.up();
                        this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
                        Debug.info("[查找粘液块位置]: 成功, " + this.slimeBlockPos);
                        Debug.info("[查找红石火把位置]: 成功, " + this.redstoneTorchBlockPos);
                    } else {
                        Messager.actionBar("bedrockminer.fail.place.redstonetorch"); // 无法放置红石火把(没有可放置的基座方块)
                        this.status = Status.FAILED;    // 失败状态
                        Debug.info("[查找粘液块位置]: 失败");
                    }
                }

            }
            case PLACE_PISTON -> {
                if (pistonBlockPos != null) {
                    Debug.info("[放置活塞]: 放置准备, " + pistonBlockPos);
                    if (!CheckingEnvironment.has2BlocksOfPlaceToPlacePiston(world, blockPos)) {
                        Messager.actionBar("bedrockminer.fail.place.piston");   // 无法放置活塞
                        this.status = Status.FAILED;
                        Debug.info("[放置活塞]: 无法放置");
                        return false;
                    }

                    InventoryManager.switchToItem(Blocks.PISTON);
                    BlockPlacer.pistonPlacement(pistonBlockPos, Direction.UP);
                    if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON)) {
                        Debug.info("[放置活塞]: 放置成功");
                    } else {
                        Debug.info("[放置活塞]: 放置失败");
                    }
                }
                this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
            }
            case PLACE_SLIME_BLOCK -> {
                if (slimeBlockPos != null) {
                    Debug.info("[放置粘液块]: 放置准备, " + slimeBlockPos);
                    BlockPlacer.simpleBlockPlacement(slimeBlockPos, Blocks.SLIME_BLOCK);
                    if (world.getBlockState(slimeBlockPos).isOf(Blocks.SLIME_BLOCK)) {
                        Debug.info("[放置粘液块]: 放置成功");
                    } else {
                        Debug.info("[放置粘液块]: 放置失败");
                    }
                }
                this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
            }
            case PLACE_REDSTONE_TORCH -> {
                if (slimeBlockPos != null && !world.getBlockState(slimeBlockPos).isOf(Blocks.SLIME_BLOCK)) {
                    Debug.info("[放置红石火把]: 需要放置粘液块, " + slimeBlockPos);
                    this.status = Status.PLACE_SLIME_BLOCK;  // 放置粘液块
                    return false;
                }
                Debug.info("[放置红石火把]: 准备放置, " + redstoneTorchBlockPos);
                BlockPlacer.simpleBlockPlacement(redstoneTorchBlockPos, Blocks.REDSTONE_TORCH);

                if (!world.getBlockState(redstoneTorchBlockPos).isAir()) {
                    Debug.info("[放置红石火把]: 放置成功");
                } else {
                    Debug.info("[放置红石火把]: 放置失败");
                }
                this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
            }
            case EXTENDED_START -> {
                if (!hasTried && pistonBlockPos != null) {
                    Debug.info("[扩展]：准备开始");
                    // 打掉活塞附近能充能的红石火把
                    ArrayList<BlockPos> nearByRedstoneTorchPosList = CheckingEnvironment.findNearbyRedstoneTorch(world, pistonBlockPos);
                    for (BlockPos pos : nearByRedstoneTorchPosList) {
                        Debug.info("[扩展]：打掉红石火把, " + pos);
                        BlockBreaker.breakBlock(pos);
                    }
                    // 打掉活塞
                    Debug.info("[扩展]：打掉活塞, " + pistonBlockPos);
                    BlockBreaker.breakBlock(pistonBlockPos);

                    // 放置朝下的活塞
                    Debug.info("[扩展]：放置朝下的活塞, " + pistonBlockPos);
                    BlockPlacer.pistonPlacement(pistonBlockPos, Direction.DOWN);

                    hasTried = true;
                    Debug.info("[扩展]：扩展完成");
                    this.status = Status.WAIT_GAME_UPDATE;  // 等待状态
                }
            }
            case PISTON_MOVING -> this.status = Status.WAIT_GAME_UPDATE;  // 等待更新状态
            case WAIT_GAME_UPDATE -> {
                return false;
            }
            case TIME_OUT -> status = Status.ITEM_RECYCLING;
            case FAILED -> {
                if (retryMax-- > 0) {
                    Debug.info("[失败]：准备重试");
                    this.status = Status.INITIALIZATION;
                } else {
                    status = Status.ITEM_RECYCLING;
                    Debug.info("[失败]：准备物品回收");
                }
            }
            case ITEM_RECYCLING -> {
                if (world.getBlockState(pistonBlockPos).isOf(Blocks.MOVING_PISTON)) {
                    Debug.info("[物品回收]: 活塞移动中");
                    return false;
                }
                if (tickRecycleTimes++ > 20) {
                    this.status = Status.FINISH;
                }
                boolean isRecyclePistonBlock = pistonBlockPos != null && world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON);
                boolean isRecycleRedstoneTorchBlock = redstoneTorchBlockPos != null && (world.getBlockState(redstoneTorchBlockPos).isOf(Blocks.REDSTONE_TORCH) || world.getBlockState(redstoneTorchBlockPos).isOf(Blocks.REDSTONE_WALL_TORCH));
                boolean isRecycleSlimeBlock = slimeBlockPos != null && world.getBlockState(slimeBlockPos).isOf(Blocks.SLIME_BLOCK);
                if (slimeBlockPos != null) {
                    Debug.info(String.format("[物品回收][%s][粘液块][BlockState]: %s", tickRecycleTimes, world.getBlockState(slimeBlockPos)));
                    Debug.info(String.format("[物品回收][%s][粘液块][Block]: %s", tickRecycleTimes, world.getBlockState(slimeBlockPos).getBlock().getName()));
                }

                // 粘液块
                if (isRecycleSlimeBlock) {
                    if (!recyclingTask.contains(slimeBlockPos)) {
                        recyclingTask.offer(slimeBlockPos);
                        Debug.info(String.format("[物品回收][%s][开始添加][粘液块]: %s", tickRecycleTimes, slimeBlockPos));
                    }
                }
                // 红石火把
                if (isRecycleRedstoneTorchBlock) {
                    if (!recyclingTask.contains(redstoneTorchBlockPos)) {
                        recyclingTask.offer(redstoneTorchBlockPos);
                        Debug.info(String.format("[物品回收][%s][开始添加][红石火把]: %s", tickRecycleTimes, redstoneTorchBlockPos));
                    }
                }
                // 活塞
                if (isRecyclePistonBlock) {
                    if (!recyclingTask.contains(pistonBlockPos)) {
                        recyclingTask.offer(pistonBlockPos);
                        Debug.info(String.format("[物品回收][%s][开始添加][活塞]: %s", tickRecycleTimes, pistonBlockPos));
                    }
                }
                // 可能残留的活塞
                if (pistonBlockPos != null) {
                    BlockPos pistonPos1 = pistonBlockPos.up();
                    if (world.getBlockState(pistonPos1).isOf(Blocks.PISTON)) {
                        if (!recyclingTask.contains(pistonPos1)) {
                            recyclingTask.offer(pistonPos1);
                            Debug.info(String.format("[物品回收][%s][开始添加][活塞up]: %s", tickRecycleTimes, pistonPos1));
                        }
                    }
                    BlockPos pistonPos2 = pistonBlockPos.up().up();
                    if (world.getBlockState(pistonPos2).isOf(Blocks.PISTON)) {
                        if (!recyclingTask.contains(pistonPos1)) {
                            recyclingTask.offer(pistonPos2);
                            Debug.info(String.format("[物品回收][%s][开始添加][活塞upup]: %s", tickRecycleTimes, pistonPos2));
                        }
                    }
                }

                if (recyclingTask.size() > 0) {
                    BlockPos pos = recyclingTask.poll();
                    if (!world.getBlockState(pos).isAir()) {
                        BlockBreaker.breakBlock(pos);
                        Debug.info(String.format("[物品回收][%s][开始回收]: %s", tickRecycleTimes, pos));
                    }
                    return false;
                }
                this.status = Status.FINISH;
            }
            case FINISH -> {
                return true;
            }
        }
        return false;
    }

    private void updateStatus() {
        // 游戏更新
        if (this.status == Status.WAIT_GAME_UPDATE) {
            // 检查活塞位置
            if (pistonBlockPos == null) {
                Debug.info("[更新状态]: 活塞坐标位置未获取");
                status = Status.FIND_PISTON_POSITION;
                return;
            }
            // 检查红石火把位置
            if (redstoneTorchBlockPos == null) {
                Debug.info("[更新状态]: 红石火把位置未获取");
                status = Status.FIND_REDSTONE_TORCH_POSITION;
                return;
            }

            // 优先检查活塞当前是否还处于技术性方块(36号方块)
            if (world.getBlockState(pistonBlockPos).isOf(Blocks.MOVING_PISTON) || world.getBlockState(pistonBlockPos.up()).isOf(Blocks.MOVING_PISTON)) {
                Debug.info("[更新状态]: 活塞移动中");
                status = Status.PISTON_MOVING;
                return;
            }

            // 检查目标方块是否存在(成功)
            if (world.getBlockState(blockPos).isAir()) {
                // 获取需要回收物品的清单
                Debug.info("[更新状态]: 目标方块已不存在, 准备执行回收任务");
                status = Status.ITEM_RECYCLING; // 物品回收状态
                return;
            }

            // 未扩展过
            if (!hasTried) {
                // 活塞存在
                if (world.getBlockState(pistonBlockPos).isOf(Blocks.PISTON)) {
                    Debug.info("[更新状态]: 活塞已放置");

                    Direction direction = world.getBlockState(pistonBlockPos).get(PistonBlock.FACING);
                    switch (direction) {
                        case UP -> {
                            // 活塞已充能(扩展开始)
                            if (world.getBlockState(pistonBlockPos).get(PistonBlock.EXTENDED)) {
                                this.status = Status.EXTENDED_START;    // 扩展开始
                                Debug.info("[更新状态]: 条件充足,准备扩展");
                            }
                            // 活塞未充能, 活塞朝向：向上(红石火把可能被之前的任务给清理掉了,需要重新放置)
                            else {
                                if (world.getBlockState(pistonBlockPos).get(PistonBlock.FACING) == Direction.UP) {
                                    this.status = Status.PLACE_REDSTONE_TORCH;  // 放置红石火把状态
                                    Debug.info("[更新状态]: 需要放置红石火把");
                                }
                            }
                        }
                        case DOWN, NORTH, SOUTH, WEST, EAST -> this.status = Status.FAILED;  // 放置失败,失败状态
                    }

                }
                // 活塞不存在
                else {
                    this.status = Status.PLACE_PISTON;   // 放置活塞状态
                    Debug.info("[更新状态]: 活塞未放置");
                }
            }
        }

    }

}


