package yan.lx.bedrockminer.utils;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BlockPlacer {
    public static void simpleBlockPlacement(BlockPos pos, ItemConvertible item) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        InventoryManager.switchToItem(item);
        BlockHitResult hitResult = new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false);
//        minecraftClient.interactionManager.interactBlock(minecraftClient.player, minecraftClient.world, Hand.MAIN_HAND, hitResult);
        placeBlockWithoutInteractingBlock(minecraftClient, hitResult);
    }

    public static void pistonPlacement(BlockPos pos, Direction direction) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        double x = pos.getX();

        switch (BreakingFlowController.getWorkingMode()) {
            case CARPET_EXTRA://carpet accurateBlockPlacement支持
                x = x + 2 + direction.getId() * 2;
                break;
            case VANILLA://直接发包，改变服务端玩家实体视角
                PlayerEntity player = minecraftClient.player;
                float pitch;
                switch (direction) {
                    case UP:
                        pitch = 90f;
                        break;
                    case DOWN:
                        pitch = -90f;
                        break;
                    default:
                        pitch = 90f;
                        break;
                }

                minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(player.getYaw(1.0f), pitch, player.isOnGround()));
                break;
        }

        Vec3d vec3d = new Vec3d(x, pos.getY(), pos.getZ());

        InventoryManager.switchToItem(Blocks.PISTON);
        BlockHitResult hitResult = new BlockHitResult(vec3d, Direction.UP, pos, false);
//        minecraftClient.interactionManager.interactBlock(minecraftClient.player, minecraftClient.world, Hand.MAIN_HAND, hitResult);
        placeBlockWithoutInteractingBlock(minecraftClient, hitResult);
    }

    private static void placeBlockWithoutInteractingBlock(MinecraftClient minecraftClient, BlockHitResult hitResult) {
        ClientPlayerEntity player = minecraftClient.player;
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);

        minecraftClient.interactionManager.sendSequencedPacket(minecraftClient.world, sequence ->
            new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, sequence));

        if (!itemStack.isEmpty() && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
            ItemUsageContext itemUsageContext = new ItemUsageContext(player, Hand.MAIN_HAND, hitResult);
            itemStack.useOnBlock(itemUsageContext);

        }
    }
}
