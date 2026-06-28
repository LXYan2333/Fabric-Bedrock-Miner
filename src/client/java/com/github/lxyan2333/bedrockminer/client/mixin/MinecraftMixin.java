package com.github.lxyan2333.bedrockminer.client.mixin;

import com.github.lxyan2333.bedrockminer.client.breaking.ClientTickScheduler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "Lnet/minecraft/client/Minecraft;handleKeybinds()V", at = @At("RETURN"))
    private void handleKeybinds(CallbackInfo ci) {
        ClientTickScheduler.INSTANCE.onTick();
    }

}
