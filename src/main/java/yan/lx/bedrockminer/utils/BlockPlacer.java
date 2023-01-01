package yan.lx.bedrockminer.utils;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
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
    /**
     * 简单方块放置
     *
     * @param pos  待放置位置
     * @param item 待放置方块
     */
    public static void simpleBlockPlacement(BlockPos pos, ItemConvertible item) {
        if (pos == null || item == null) {
            return;
        }

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;
        ClientPlayNetworkHandler clientPlayNetworkHandler = minecraftClient.getNetworkHandler();
        if (player == null || clientPlayNetworkHandler == null) {
            return;
        }

        Direction direction = Direction.UP;
        float pitch = 90f;
        minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(player.getYaw(1.0f), pitch, player.isOnGround()));

        InventoryManager.switchToItem(item);
        BlockHitResult hitResult = new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), direction, pos, false);

        placeBlockWithoutInteractingBlock(hitResult);
    }

    /**
     * 活塞放置
     *
     * @param pos       活塞放置坐标
     * @param direction 活塞放置方向
     */
    public static void pistonPlacement(BlockPos pos, Direction direction) {
        if (pos == null || direction == null) {
            return;
        }

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;
        ClientPlayNetworkHandler clientPlayNetworkHandler = minecraftClient.getNetworkHandler();

        if (player == null || clientPlayNetworkHandler == null) {
            return;
        }

        float pitch = switch (direction) {
            case UP, NORTH, SOUTH, WEST, EAST -> 90f;
            case DOWN -> -90f;
        };
        minecraftClient.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(player.getYaw(1.0f), pitch, player.isOnGround()));

        Vec3d vec3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

        InventoryManager.switchToItem(Blocks.PISTON);
        BlockHitResult hitResult = new BlockHitResult(vec3d, Direction.UP, pos, false);

        placeBlockWithoutInteractingBlock(hitResult);
    }

    /**
     * 放置没有交互的方块
     */
    private static void placeBlockWithoutInteractingBlock(BlockHitResult hitResult) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientWorld world = minecraftClient.world;
        PlayerEntity player = minecraftClient.player;
        ClientPlayerInteractionManager interactionManager = minecraftClient.interactionManager;
        if (world == null || player == null || interactionManager == null) {
            return;
        }

        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);

        interactionManager.sendSequencedPacket(minecraftClient.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, sequence));

        if (!itemStack.isEmpty() && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
            ItemUsageContext itemUsageContext = new ItemUsageContext(player, Hand.MAIN_HAND, hitResult);
            itemStack.useOnBlock(itemUsageContext);
        }
    }
}
