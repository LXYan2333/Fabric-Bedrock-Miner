package com.github.lxyan2333.bedrockminer.client.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class BedrockMinerMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".ServerboundMovePlayerPacketMixin")) {
            //? if >=1.17 {
            return true;
            //?} else {
            /*return false;
            *///?}
        }

        if (mixinClassName.endsWith(".legacy.ServerboundMovePlayerPacketPosRotMixin")
                || mixinClassName.endsWith(".legacy.ServerboundMovePlayerPacketRotMixin")) {
            //? if <1.17 {
            /*return true;
            *///?} else
            return false;
        }

        if (mixinClassName.endsWith(".legacy.GuiTextFieldGenericMixin")) {
            //? if >=1.17 && <=1.19.2 {
            /*return true;
            *///?} else {
            return false;
            //?}
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
