package yan.lx.bedrockminer.mixins;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import yan.lx.bedrockminer.utils.BreakingFlowController;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

//    @Inject(at = @At("HEAD"), method = "doAttack")
//    private void init(CallbackInfo info) {
//    }

    @Shadow
    @Nullable
    public ClientWorld world;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    @Nullable
    public HitResult crosshairTarget;
    private Window window;

    @Inject(method = "doItemUse", at = @At(value = "HEAD"))
    private void onInitComplete(CallbackInfo ci) {
        if (this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
            if (world.getBlockState(blockHitResult.getBlockPos()).isOf(Blocks.BEDROCK) && player.getMainHandStack().isEmpty()) {
                BreakingFlowController.switchOnOff();
            }
        }

    }


    @Inject(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void inject(boolean bl, CallbackInfo ci, BlockHitResult blockHitResult, BlockPos blockPos, Direction direction) {
        if (world.getBlockState(blockPos).isOf(Blocks.BEDROCK) && BreakingFlowController.isWorking()) {
            BreakingFlowController.addBlockPosToList(blockPos);
        }


    }
}

