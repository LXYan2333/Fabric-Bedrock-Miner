package yan.lx.bedrockminer.keybinding;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import yan.lx.bedrockminer.utils.BreakingFlowController;

public class Keybinds implements ClientModInitializer {

   private static KeyBinding toggleKeybind;
    
    @Override
    public void onInitializeClient() {
        toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("Toggle Bedrock Miner", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "Bedrock Miner"));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
          while (toggleKeybind.wasPressed()) {
            BreakingFlowController.switchOnOff();
          }
		    });
    }
}