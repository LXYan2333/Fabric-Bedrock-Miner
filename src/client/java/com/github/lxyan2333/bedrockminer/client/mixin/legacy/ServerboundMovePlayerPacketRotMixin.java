package com.github.lxyan2333.bedrockminer.client.mixin.legacy;

import com.github.lxyan2333.bedrockminer.client.breaking.approach.VanillaAllDirectionApproach;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundMovePlayerPacket.Rot.class)
//? if <1.17 {
/*public abstract class ServerboundMovePlayerPacketRotMixin extends ServerboundMovePlayerPacket {
*///?} else
public abstract class ServerboundMovePlayerPacketRotMixin {
    //? if <1.17 {
    /*@Inject(method = "<init>(FFZ)V", at = @At("RETURN"))
    private void bedrockMiner$modifyRotation(float originalYRot, float originalXRot, boolean onGround, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) {
            return;
        }

        if (VanillaAllDirectionApproach.currentYawPitch != null) {
            this.yRot = VanillaAllDirectionApproach.currentYawPitch.getFirst();
            this.xRot = VanillaAllDirectionApproach.currentYawPitch.getSecond();
        }
    }
    *///?}
}
