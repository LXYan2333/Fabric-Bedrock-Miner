package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.network.ClientPlayerEntity;
//import net.minecraft.entity.Entity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
//import net.minecraft.util.math.Position;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
//import java.util.List;

public class BreakingFlowController {
    private static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();
    public static ArrayList<Block> allowBreakBlockList = new ArrayList<>();

    public static boolean isWorking() {
        return working;
    }

    private static boolean working = false;

    static {
        allowBreakBlockList.add(Blocks.BEDROCK);            // 基岩
        allowBreakBlockList.add(Blocks.OBSIDIAN);           // 黑曜石
        allowBreakBlockList.add(Blocks.END_PORTAL);         // 末地传送门
        allowBreakBlockList.add(Blocks.END_PORTAL_FRAME);   // 末地传送门-框架
        allowBreakBlockList.add(Blocks.END_GATEWAY);        // 末地折跃门
    }

    public static void onInitComplete(ClientWorld world, HitResult crosshairTarget, @Nullable ClientPlayerEntity player) {
        BlockHitResult blockHitResult = (BlockHitResult) crosshairTarget;
        for (Block block : allowBreakBlockList) {
            if (world.getBlockState(blockHitResult.getBlockPos()).isOf(block) && player.getMainHandStack().isEmpty()) {
                BreakingFlowController.switchOnOff();
                break;
            }
        }
    }

    public static void onHandleBlockBreaking(ClientWorld world, BlockPos blockPos) {
        for (Block block : allowBreakBlockList) {
            if (world.getBlockState(blockPos).isOf(block) && BreakingFlowController.isWorking()) {
                BreakingFlowController.addBlockPosToList(blockPos);
                break;
            }
        }
    }

    public static void addBlockPosToList(BlockPos pos) {
        ClientWorld world = MinecraftClient.getInstance().world;
        Block block = null;
        for (Block block1 : allowBreakBlockList) {
            if (world.getBlockState(pos).isOf(block1)) {
                block = block1;
                break;
            }
        }
        if (block == null) {
            Messager.rawactionBar("请确保敲击的方块是(基岩|黑曜石|末地传送门)其中一个！");
        }

        String haveEnoughItems = InventoryManager.warningMessage();
        if (haveEnoughItems != null) {
            Messager.actionBar(haveEnoughItems);
            return;
        }
        if (shouldAddNewTargetBlock(pos)) {
            TargetBlock targetBlock = new TargetBlock(pos, world);
            cachedTargetBlockList.add(targetBlock);
        }

    }


    public static void tick() {
        if (InventoryManager.warningMessage() != null) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;

        if (!minecraftClient.interactionManager.getCurrentGameMode().isSurvivalLike()) {
            return;
        }

        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);

            // 玩家切换世界
            if (selectedBlock.getWorld() != MinecraftClient.getInstance().world) {
                cachedTargetBlockList.clear();  // 清空所有缓存的任务
                break;
            }

            if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 3.4f)) {
                TargetBlock.Status status = cachedTargetBlockList.get(i).tick();
                // 判断任务是否执行完毕
                if (status == TargetBlock.Status.Finish) {
                    cachedTargetBlockList.remove(i); // 删除当前任务
                } else {
                    break;
                }

            }
        }
    }

    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
        return blockPos.isWithinDistance(player.getPos(), range);
    }

    public static WorkingMode getWorkingMode() {
        return WorkingMode.VANILLA;
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos) {
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos().getManhattanDistance(pos) == 0) {
                return false;
            }
        }
        return true;
    }

    public static void switchOnOff() {
        if (working) {
            Messager.chat("bedrockminer.toggle.off");

            working = false;
        } else {
            Messager.chat("bedrockminer.toggle.on");

            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (!minecraftClient.isInSingleplayer()) {

                Messager.chat("bedrockminer.warn.multiplayer");
            }
            working = true;
        }
    }


    //测试用的。使用原版模式已经足以满足大多数需求。
//just for test. The VANILLA mode is powerful enough.
    enum WorkingMode {
        CARPET_EXTRA,
        VANILLA,
        MANUALLY;
    }
}
