package yan.lx.bedrockminer.utils;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.network.ClientPlayerEntity;
//import net.minecraft.entity.Entity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
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
        if (world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();

            String haveEnoughItems = InventoryManager.warningMessage();
            if (haveEnoughItems != null) {
                Messager.actionBar(haveEnoughItems);
                return;
            }

            if (shouldAddNewTargetBlock(pos)){
                TargetBlock targetBlock = new TargetBlock(pos, world);
                cachedTargetBlockList.add(targetBlock);
            }
        } else {
            //Does not Have an english version, Left out of lang for now. (raw To prevent Errors)
            Messager.rawactionBar("请确保敲击的方块还是基岩！");
        }
    }

    public static void tick() {
        if (InventoryManager.warningMessage() != null) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;
        ClientPlayNetworkHandler networkHandler = minecraftClient.getNetworkHandler();

        if (!"survival".equals(minecraftClient.interactionManager.getCurrentGameMode().getName())) {
            return;
        }

        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);
            //玩家切换世界，或离目标方块太远时，删除所有缓存的任务
            if (selectedBlock.getWorld() != MinecraftClient.getInstance().world ) {
                cachedTargetBlockList.clear();
                break;
            }
            if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 3.4f)) {
                TargetBlock.Status status = cachedTargetBlockList.get(i).tick();
                if (status == TargetBlock.Status.RETRACTING) {
                    continue;
                }
                if (status == TargetBlock.Status.FAILED || status == TargetBlock.Status.COMPLETE) {
                    cachedTargetBlockList.remove(i);
                    networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,selectedBlock.getBlockPos(), Direction.UP));
                } else {
                    break;
                }
            }
        }
    }

    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
        return blockPos.isWithinDistance(player.getEyePos(), range);
    }

    public static WorkingMode getWorkingMode() {
        return WorkingMode.VANILLA;
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos){
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos().getManhattanDistance(pos) == 0){
                return false;
            }
        }
        return true;
    }

    public static void switchOnOff(){
        if (working){
            Messager.chat("bedrockminer.toggle.off");

            working = false;
        } else {
            Messager.chat("bedrockminer.toggle.on");

            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (!minecraftClient.isInSingleplayer()){

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
