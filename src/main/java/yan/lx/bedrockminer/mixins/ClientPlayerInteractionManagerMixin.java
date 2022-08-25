package yan.lx.bedrockminer.mixins;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yan.lx.bedrockminer.utils.BreakingFlowController;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    /*** 交互更新 ***/
    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo info) {
        BreakingFlowController.tick();
    }
}
