package com.github.lxyan2333.bedrockminer.client.mixin.legacy;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiTextFieldGeneric.class)
public abstract class GuiTextFieldGenericMixin extends EditBox {
    public GuiTextFieldGenericMixin(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    /**
     * Older MaLiLib versions call EditBox#moveCursorTo here. In Mojang mappings that method
     * virtual-dispatches back to setCursorPosition(), causing infinite recursion.
     */
    @Overwrite(remap = false)
    public void setCursorPosition(int pos) {
        super.setCursorPosition(pos);
    }
}
