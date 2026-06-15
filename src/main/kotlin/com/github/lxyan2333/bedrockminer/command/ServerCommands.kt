package com.github.lxyan2333.bedrockminer.command

import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.config.ServerConfigManager
import com.github.lxyan2333.bedrockminer.network.ModNetwork
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.IdentifierArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.permissions.Permissions

object ServerCommands {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("bedrock-miner")
                .requires { source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN) }.then(
                    Commands.literal("blocklist").then(
                        Commands.literal("add").then(
                            Commands.argument("block", IdentifierArgument.id()).executes { context ->
                                val block = IdentifierArgument.getId(context, "block")
                                ServerConfigData.serverBlockList.add(block.toString())
                                ServerConfigManager.save()
                                broadcastConfig(context.source.server)
                                context.source.sendSuccess(
                                    { Component.literal("Added $block to block list") }, true
                                )
                                1
                            })
                    ).then(
                        Commands.literal("remove").then(
                            Commands.argument("block", IdentifierArgument.id()).executes { context ->
                                val block = IdentifierArgument.getId(context, "block")
                                ServerConfigData.serverBlockList.remove(block.toString())
                                ServerConfigManager.save()
                                broadcastConfig(context.source.server)
                                context.source.sendSuccess(
                                    { Component.literal("Removed $block from block list") }, true
                                )
                                1
                            })
                    ).then(
                        Commands.literal("clear").executes { context ->
                            ServerConfigData.serverBlockList.clear()
                            ServerConfigManager.save()
                            broadcastConfig(context.source.server)
                            context.source.sendSuccess(
                                { Component.literal("Cleared block list") }, true
                            )
                            1
                        }).then(
                        Commands.literal("list").executes { context ->
                            val list = ServerConfigData.serverBlockList.ifEmpty { "(empty)" }
                            context.source.sendSuccess(
                                { Component.literal("Block list: $list") }, false
                            )
                            1
                        })
                ).then(
                    Commands.literal("allowlist").then(
                        Commands.literal("add").then(
                            Commands.argument("block", IdentifierArgument.id()).executes { context ->
                                val block = IdentifierArgument.getId(context, "block")
                                ServerConfigData.serverAllowList.add(block.toString())
                                ServerConfigManager.save()
                                broadcastConfig(context.source.server)
                                context.source.sendSuccess(
                                    { Component.literal("Added $block to allow list") }, true
                                )
                                1
                            })
                    ).then(
                        Commands.literal("remove").then(
                            Commands.argument("block", IdentifierArgument.id()).executes { context ->
                                val block = IdentifierArgument.getId(context, "block")
                                ServerConfigData.serverAllowList.remove(block.toString())
                                ServerConfigManager.save()
                                broadcastConfig(context.source.server)
                                context.source.sendSuccess(
                                    { Component.literal("Removed $block from allow list") }, true
                                )
                                1
                            })
                    ).then(
                        Commands.literal("clear").executes { context ->
                            ServerConfigData.serverAllowList.clear()
                            ServerConfigManager.save()
                            broadcastConfig(context.source.server)
                            context.source.sendSuccess(
                                { Component.literal("Cleared allow list") }, true
                            )
                            1
                        }).then(
                        Commands.literal("list").executes { context ->
                            val list = ServerConfigData.serverAllowList.ifEmpty { "(empty)" }
                            context.source.sendSuccess(
                                { Component.literal("Allow list: $list") }, false
                            )
                            1
                        })
                ).then(
                    Commands.literal("mode")
                        .then(Commands.argument("mode", StringArgumentType.word()).suggests { _, builder ->
                            SharedSuggestionProvider.suggest(
                                listOf("BLOCKED", "ALLOWED"), builder
                            )
                            builder.buildFuture()
                        }.executes { context ->
                            val mode = StringArgumentType.getString(context, "mode").uppercase()
                            if (mode != "BLOCKED" && mode != "ALLOWED") {
                                context.source.sendFailure(
                                    Component.literal("Invalid mode. Use BLOCKED or ALLOWED.")
                                )
                                return@executes 0
                            }
                            ServerConfigData.serverBlockListMode = mode
                            ServerConfigManager.save()
                            broadcastConfig(context.source.server)
                            context.source.sendSuccess(
                                { Component.literal("Set block list mode to $mode") }, true
                            )
                            1
                        })
                ).then(
                    Commands.literal("reload").executes { context ->
                        ServerConfigManager.load()
                        broadcastConfig(context.source.server)
                        context.source.sendSuccess(
                            { Component.literal("Reloaded server config from disk") }, true
                        )
                        1
                    }).then(
                    Commands.literal("show").executes { context ->
                        context.source.sendSuccess(
                            { Component.literal("§7Block list:§r ${ServerConfigData.serverBlockList.ifEmpty { "(empty)" }}") },
                            false
                        )
                        context.source.sendSuccess(
                            { Component.literal("§7Allow list:§r ${ServerConfigData.serverAllowList.ifEmpty { "(empty)" }}") },
                            false
                        )
                        context.source.sendSuccess(
                            { Component.literal("§7Block list mode:§r ${ServerConfigData.serverBlockListMode}") },
                            false
                        )
                        context.source.sendSuccess(
                            { Component.literal("§7Protocol version:§r ${ServerConfigData.PROTOCOL_VERSION}") },
                            false
                        )
                        1
                    })
            )
        }
    }

    private fun broadcastConfig(server: net.minecraft.server.MinecraftServer) {
        val payload = ModNetwork.ConfigSyncPayload(
            ServerConfigData.PROTOCOL_VERSION,
            ServerConfigData.serverBlockList,
            ServerConfigData.serverAllowList,
            ServerConfigData.serverBlockListMode,
        )
        for (player in server.playerList.players) {
            if (ServerPlayNetworking.canSend(player, ModNetwork.ConfigSyncPayload.TYPE)) {
                ServerPlayNetworking.send(player, payload)
            }
        }
    }
}
