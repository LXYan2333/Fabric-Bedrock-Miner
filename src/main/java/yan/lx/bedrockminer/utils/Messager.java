package yan.lx.bedrockminer.utils;


import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Messager {
    public static void actionBar(String message) {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.translatable(message), false);
    }

    public static void rawactionBar(String message) {
        Text text = Text.literal(message);
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.literal(message), false);
    }

    public static void chat(String message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable(message));
    }

    public static void rawchat(String message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(message));
    }
}

