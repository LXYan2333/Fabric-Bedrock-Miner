package yan.lx.bedrockminer.mixins;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yan.lx.bedrockminer.utils.BreakingFlowController;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo info) {
        if (BreakingFlowController.isWorking()) {
            BreakingFlowController.tick();
        }
    }
}
