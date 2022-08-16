package yan.lx.bedrockminer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
//import java.util.List;

public class BreakingFlowController {
    private static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();
    private static ArrayList<Block> allowBlockList = new ArrayList<>();
    private static ArrayList<String> allowBlockNameList = new ArrayList<>();

    public static boolean isWorking() {
        return working;
    }

    public static String getBlocksName() {
        return String.join("|", allowBlockNameList);
    }

    private static boolean working = false;

    static {
        allowBlockList.add(Blocks.BEDROCK);            // 基岩
        allowBlockList.add(Blocks.OBSIDIAN);           // 黑曜石
        allowBlockList.add(Blocks.END_PORTAL);         // 末地传送门
        allowBlockList.add(Blocks.END_PORTAL_FRAME);   // 末地传送门-框架
        allowBlockList.add(Blocks.END_GATEWAY);        // 末地折跃门
        // 添加已支持方块名称(输出文本使用)
        for (Block block : allowBlockList) {
            allowBlockNameList.add(block.getName().getString());
        }
    }

    public static void addBlockPosToList(BlockPos pos) {
        ClientWorld world = MinecraftClient.getInstance().world;
        Block block = null;
        for (Block allowBlock : allowBlockList) {
            if (world.getBlockState(pos).isOf(allowBlock)) {
                block = allowBlock;
                break;
            }
        }
        if (block == null) {
            Messager.rawactionBar("请确保敲击的方块还是" + getBlocksName() + "！");
        }

        String haveEnoughItems = InventoryManager.warningMessage();
        if (haveEnoughItems != null) {
            Messager.actionBar(haveEnoughItems);
            return;
        }

        if (shouldAddNewTargetBlock(pos)) {
            TargetBlock targetBlock = new TargetBlock(block, pos, world);
            cachedTargetBlockList.add(targetBlock);
        }
    }

    public static void tick() {
        if (InventoryManager.warningMessage() != null) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;

        if (!"survival".equals(minecraftClient.interactionManager.getCurrentGameMode().getName())) {
            return;
        }

        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);

            //玩家切换世界，或离目标方块太远时，删除所有缓存的任务
            if (selectedBlock.getWorld() != MinecraftClient.getInstance().world) {
                cachedTargetBlockList = new ArrayList<TargetBlock>();
                break;
            }

            if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 3.4f)) {
                TargetBlock.Status status = cachedTargetBlockList.get(i).tick();
                if (status == TargetBlock.Status.RETRACTING) {
                    continue;
                } else if (status == TargetBlock.Status.FAILED || status == TargetBlock.Status.RETRACTED) {
                    cachedTargetBlockList.remove(i);
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

    public static void switchOnOff(@Nullable ClientWorld world, BlockHitResult blockHitResult, @Nullable ClientPlayerEntity player) {
        Block block = null;
        for (Block allowBlock : allowBlockList) {
            if (world.getBlockState(blockHitResult.getBlockPos()).isOf(allowBlock)) {
                block = allowBlock;
                break;
            }
        }
        if (block == null) {
            return;
        }

        if (working) {
            Messager.chat("bedrockminer.toggle.off");

            working = false;
        } else {
            Messager.chat("bedrockminer.toggle.on");
            Messager.rawchat(getBlocksName());
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
