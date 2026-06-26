package com.github.lxyan2333.bedrockminer.client.mixin;

import com.github.lxyan2333.bedrockminer.client.breaking.approach.VanillaAllDirectionApproach;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.17
@Mixin(ServerboundMovePlayerPacket.class)
public abstract class ServerboundMovePlayerPacketMixin {

    @Shadow
    @Final
    private boolean hasRot;

    @Shadow
    @Final
    @Mutable
    private float yRot;

    @Shadow
    @Final
    @Mutable
    private float xRot;

    //? if >=1.21.11 {
    @Inject(method = "<init>(DDDFFZZZZ)V", at = @At("RETURN"))
    private void bedrockMiner$modifyRotation(double x, double y, double z, float originalYRot, float originalXRot, boolean onGround, boolean horizontalCollision, boolean hasPos, boolean hasRotArgument, CallbackInfo ci) {
        bedrockMiner$modifyRotation();
    }
    //?} else if >=1.17 {
    /*@Inject(method = "<init>(DDDFFZZZ)V", at = @At("RETURN"))
    private void bedrockMiner$modifyRotation(double x, double y, double z, float originalYRot, float originalXRot, boolean onGround, boolean hasPos, boolean hasRotArgument, CallbackInfo ci) {
        bedrockMiner$modifyRotation();
    }
    *///?}

    private void bedrockMiner$modifyRotation() {
        if (!this.hasRot) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isSameThread()) {
            return;
        }

        if (VanillaAllDirectionApproach.currentYawPitch != null) {
            this.yRot = VanillaAllDirectionApproach.currentYawPitch.getFirst();
            this.xRot = VanillaAllDirectionApproach.currentYawPitch.getSecond();
        }
    }
}
