package yan.lx.bedrockminer.utils;


import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.UUID;

public class Messager {
    public static void actionBar(String message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Text text = new LiteralText(message);
        minecraftClient.inGameHud.setOverlayMessage(text,false);
    }

    public static void chat(String message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Text text = new LiteralText(message);
        minecraftClient.inGameHud.addChatMessage(MessageType.SYSTEM,text, UUID.randomUUID());
    }
}

