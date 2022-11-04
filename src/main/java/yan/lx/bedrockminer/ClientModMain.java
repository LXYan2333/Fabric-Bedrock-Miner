package yan.lx.bedrockminer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import yan.lx.bedrockminer.utils.BreakingFlowController;

public class ClientModMain implements ClientModInitializer {

    public void registerCommands() {
        var cmd1 = ClientCommandManager.literal("toggle").executes(context -> {
            BreakingFlowController.switchOnOff();
            return 0;
        });
        var c = ClientCommandManager
                .literal("bedrockbreaker")
                .then(cmd1);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(c);
        });
    }
    @Override
    public void onInitializeClient() {
        registerCommands();
    }
}
