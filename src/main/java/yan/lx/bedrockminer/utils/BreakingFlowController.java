package yan.lx.bedrockminer.utils;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.network.ClientPlayerEntity;
//import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.world.ClientWorld;
//import net.minecraft.util.math.Position;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
//import java.util.List;

public class BreakingFlowController {
    private static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();

    public static boolean isWorking() {
        return working;
    }

    private static boolean working = false;


    public static void addBlockPosToList(BlockPos pos) {
        ClientWorld world = MinecraftClient.getInstance().world;
            MinecraftClient minecraftClient = MinecraftClient.getInstance();

            String haveEnoughItems = InventoryManager.warningMessage();
            if (haveEnoughItems != null) {
                Messager.actionBar(haveEnoughItems);
                return;
            }

            if (shouldAddNewTargetBlock(pos)){
                TargetBlock targetBlock = new TargetBlock(pos, world);
                cachedTargetBlockList.add(targetBlock);
                System.out.println("新任务");
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
            if (selectedBlock.getWorld() != MinecraftClient.getInstance().world ) {
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
        return (blockPos.getSquaredDistance(player.getPos(), true) <= range * range);
    }

    public static WorkingMode getWorkingMode() {
        return WorkingMode.VANILLA;
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos){
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos().getSquaredDistance(pos.getX(),pos.getY(),pos.getZ(),false) == 0){
                return false;
            }
        }
        return true;
    }

    public static void switchOnOff(){
        if (working){
            Messager.chat("");
            Messager.chat("Bedrock Miner已关闭。");
            Messager.chat("Bedrock Miner stopped.");
            Messager.chat("");
            working = false;
        } else {
            Messager.chat("");
            Messager.chat("§7╔════════════════════════════════╗§r");
            Messager.chat("§7║§r Bedrock Miner已启动！左键基岩即可自动破除基岩。               §7║§r");
            Messager.chat("§7║§r                                                                        §7║§r");
            Messager.chat("§7║§r Bedrock Miner started! Left click bedrock to break it.    §7║§r");
            Messager.chat("§7╚════════ Author: LXYan 作者: LXYan ═════════╝§r");
            Messager.chat("");
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (!minecraftClient.isInSingleplayer()){
                Messager.chat("§7看起来你好像是在服务器使用Bedrock Miner？§r");
                Messager.chat("§7在使用本mod前请先征询其他玩家的意见。§r");
                Messager.chat("§7It seems that you are playing on a server. §r");
                Messager.chat("§7Please ask other player's opinions first.§r");
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
