package yan.lx.bedrockminer;

import dev.xpple.clientarguments.arguments.CBlockStateArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import yan.lx.bedrockminer.utils.BlockBreaker;

@Environment(EnvType.CLIENT)
public class TweakedFBMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("addBlock")
                    .then(ClientCommandManager.argument("Block", CBlockStateArgumentType.blockState(registryAccess))
                            .executes((ctx)->{
                                Block b = CBlockStateArgumentType.getCBlockState(ctx, "Block").getBlockState().getBlock();
                                if(BlockBreaker.blocksOfInterest.contains(b)){
                                    MinecraftClient.getInstance().player.sendMessage(Text.of("Block already in list"));
                                }else{
                                    BlockBreaker.blocksOfInterest.add(b);
                                    MinecraftClient.getInstance().player.sendMessage(Text.of("Block added to list"));
                                }
                                return 0;
                            })));

            dispatcher.register(ClientCommandManager.literal("removeBlock")
                    .then(ClientCommandManager.argument("Block",CBlockStateArgumentType.blockState(registryAccess))
                            .executes(ctx -> {
                                Block b = CBlockStateArgumentType.getCBlockState(ctx, "Block").getBlockState().getBlock();
                                if(BlockBreaker.blocksOfInterest.contains(b))
                                    BlockBreaker.blocksOfInterest.remove(b);
                                return 0;
                            })));

            dispatcher.register(ClientCommandManager.literal("list")
                    .executes(ctx -> {
                        StringBuilder s = new StringBuilder();
                        for (Block block : BlockBreaker.blocksOfInterest) {
                            String tempS = block.toString().replace("minecraft:","").replace("Block","").replace("{","").replace("}","");
                            s.append(tempS+"; ");
                        }
                        MinecraftClient.getInstance().player.sendMessage(Text.of("Current List of Blocks to Mine:"));
                        MinecraftClient.getInstance().player.sendMessage(Text.of(s.toString()));
                        return 0;
                    }));
        }));
    }
}