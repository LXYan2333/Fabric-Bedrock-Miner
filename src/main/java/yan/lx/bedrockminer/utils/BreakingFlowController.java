package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class BreakingFlowController {
    private static int tickNum = 0;
    private static List<Block> allowBlockList = new LinkedList<>();
    private static List<TargetBlock> cachedTargetBlockList = new LinkedList<>();
    private static boolean working = false;

    static {
        allowBlockList.add(Blocks.BEDROCK);            // 基岩
        allowBlockList.add(Blocks.OBSIDIAN);           // 黑曜石
        allowBlockList.add(Blocks.END_PORTAL);         // 末地传送门
        allowBlockList.add(Blocks.END_PORTAL_FRAME);   // 末地传送门-框架
        allowBlockList.add(Blocks.END_GATEWAY);        // 末地折跃门
    }

    public static void onDoItemUse(HitResult crosshairTarget, ClientWorld world, ClientPlayerEntity player) {
        if (crosshairTarget.getType() != HitResult.Type.BLOCK || !player.getMainHandStack().isEmpty()) {
            return;
        }

        @Nullable Block block = null;
        BlockHitResult blockHitResult = (BlockHitResult) crosshairTarget;
        for (Block allowBlock : allowBlockList) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (world.getBlockState(blockPos).isOf(allowBlock)) {
                block = allowBlock;
                break;
            }
        }
        if (block == null) {
            // Messager.rawactionBar("当前方块不在支持列表中");
            return;
        }
        switchOnOff();
    }

    public static void onHandleBlockBreaking(ClientWorld world, BlockPos blockPos) {
        if (!working) {
            return;
        }
        for (Block block : allowBlockList) {
            if (world.getBlockState(blockPos).isOf(block)) {
                addBlockPosToList(blockPos);
                return;
            }
        }
    }

    public static WorkingMode getWorkingMode() {
        return WorkingMode.VANILLA;
    }

    public static void switchOnOff() {
        if (working) {
            Messager.chat("bedrockminer.toggle.off");
            working = false;
        } else {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            // 判断玩家是否为创造
            if (minecraftClient.interactionManager.getCurrentGameMode().isCreative()) {
                Messager.chat("bedrockminer.fail.missing.survival");
                return;
            }
            Messager.chat("bedrockminer.toggle.on");
            // 判断是否在服务器
            if (!minecraftClient.isInSingleplayer()) {
                Messager.chat("bedrockminer.warn.multiplayer");
            }
            working = true;
        }
    }


    public static void addBlockPosToList(BlockPos pos) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientWorld world = minecraftClient.world;
        if (world == null) return;

        // 判断部分开启条件
        String haveEnoughItems = InventoryManager.warningMessage();
        if (haveEnoughItems != null) {
            Messager.actionBar(haveEnoughItems);
            return;
        }

        // 添加新目标方块到任务列表
        if (shouldAddNewTargetBlock(pos)) {
            Block block = world.getBlockState(pos).getBlock();
            TargetBlock targetBlock = new TargetBlock(block, pos, world);
            cachedTargetBlockList.add(targetBlock);
        }
    }


    public static void tick() {
        if (!working) return;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientWorld world = minecraftClient.world;
        PlayerEntity player = minecraftClient.player;
        ClientPlayerInteractionManager interactionManager = minecraftClient.interactionManager;
        if (world == null || player == null || interactionManager == null) {
            return;
        }
        // 运行更新程序
        updater(minecraftClient, world, player, interactionManager);
    }

    public static void updater(MinecraftClient minecraftClient, ClientWorld world, PlayerEntity player, ClientPlayerInteractionManager interactionManager) {
        if (InventoryManager.warningMessage() != null) {
            return;
        }
        if (interactionManager.getCurrentGameMode().isCreative()) {
            return;
        }
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);
            // 玩家切换世界,距离目标方块太远时,删除所有缓存的任务
            if (selectedBlock.getWorld() != world) {
                cachedTargetBlockList.clear();
                break;
            }
            // 判断玩家与方块距离是否在处理范围内
            if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 3.4f)) {
                boolean finish = cachedTargetBlockList.get(i).updater();
                if (finish) {
                    cachedTargetBlockList.remove(i);
                    i--;
                }
            }
        }
    }


    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
        return blockPos.isWithinDistance(player.getPos(), range);
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos) {
        for (TargetBlock targetBlock : cachedTargetBlockList) {
            if (targetBlock.getBlockPos().getManhattanDistance(pos) == 0) {
                return false;
            }
        }
        return true;
    }


    /*** 测试用的。使用原版模式已经足以满足大多数需求。 ***/
    public enum WorkingMode {
        CARPET_EXTRA,
        VANILLA,
        MANUALLY;
    }
}
