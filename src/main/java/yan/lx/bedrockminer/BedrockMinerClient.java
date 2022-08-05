package yan.lx.bedrockminer;

import dev.xpple.clientarguments.arguments.CBlockStateArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import yan.lx.bedrockminer.utils.BlockBreaker;

public class BedrockMinerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //add block command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess)->{
            dispatcher.register(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("block", CBlockStateArgumentType.blockState(registryAccess))
                            .executes(ctx -> {
                                BlockState bs = CBlockStateArgumentType.getCBlockState(ctx, "block").getBlockState();
                                if(!BlockBreaker.blocksOfInterest.contains(bs.getBlock())) {
                                    BlockBreaker.blocksOfInterest.add(bs.getBlock());
                                    MinecraftClient.getInstance().player.sendMessage(Text.of("Block added to list"));
                                }
                                else
                                    MinecraftClient.getInstance().player.sendMessage(Text.of("Block already in list"));
                                return 0;
                            })));
        });

        //remove block command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess)->{
            dispatcher.register(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("block", CBlockStateArgumentType.blockState(registryAccess))
                            .executes(ctx -> {
                                BlockState bs = CBlockStateArgumentType.getCBlockState(ctx, "block").getBlockState();
                                if(BlockBreaker.blocksOfInterest.contains(bs.getBlock())) {
                                    BlockBreaker.blocksOfInterest.remove(bs.getBlock());
                                    MinecraftClient.getInstance().player.sendMessage(Text.of("Block removed from list"));
                                }
                                else
                                    MinecraftClient.getInstance().player.sendMessage(Text.of("Block already not in list"));
                                return 0;
                            })));
        });

        //list all removable blocks
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess)->{
            dispatcher.register(ClientCommandManager.literal("list")
                        .executes(ctx -> {
                            StringBuilder s = new StringBuilder();
                            for (Block block : BlockBreaker.blocksOfInterest) {
                                s.append(block.toString().replace("minecraft:",""));
                                s.append(", ");
                            }
                            MinecraftClient.getInstance().player.sendMessage(Text.of("Following blocks are in the list of removable blocks:"));
                            MinecraftClient.getInstance().player.sendMessage(Text.of(s.toString()));
                            return 0;
                        }));
        });
    }
}