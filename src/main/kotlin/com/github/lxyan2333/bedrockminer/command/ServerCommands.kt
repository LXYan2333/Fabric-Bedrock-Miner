package com.github.lxyan2333.bedrockminer.command

import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.config.ServerConfigManager
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.IdentifierArgument
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.permissions.Permissions

object ServerCommands {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("bedrock-miner")
                    .requires { source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN) }.then(
                        Commands.literal("blocklist").then(
                            Commands.literal("add")
                                .then(Commands.argument("block", IdentifierArgument.id()).suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        BuiltInRegistries.BLOCK.keySet(), builder
                                    )
                                }.executes { context ->
                                    val block = IdentifierArgument.getId(context, "block").toString()
                                    if (!ServerConfigManager.addToBlockList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Invalid block name: $block")
                                        )
                                        return@executes 0
                                    }
                                    context.source.sendSuccess(
                                        { Component.literal("Added $block to block list") }, true
                                    )
                                    1
                                })
                        ).then(
                            Commands.literal("remove")
                                .then(Commands.argument("block", IdentifierArgument.id()).suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        ServerConfigData.serverBlockList.mapNotNull { net.minecraft.resources.Identifier.tryParse(it) },
                                        builder
                                    )
                                }.executes { context ->
                                    val block = IdentifierArgument.getId(context, "block").toString()
                                    if (!ServerConfigManager.removeFromBlockList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Block $block is not in block list")
                                        )
                                        return@executes 0
                                    }
                                    context.source.sendSuccess(
                                        { Component.literal("Removed $block from block list") }, true
                                    )
                                    1
                                })
                        ).then(
                            Commands.literal("clear").executes { context ->
                                ServerConfigManager.clearBlockList()
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
                            Commands.literal("add")
                                .then(Commands.argument("block", IdentifierArgument.id()).suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        BuiltInRegistries.BLOCK.keySet(), builder
                                    )
                                }.executes { context ->
                                    val block = IdentifierArgument.getId(context, "block").toString()
                                    if (!ServerConfigManager.addToAllowList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Invalid block name: $block")
                                        )
                                        return@executes 0
                                    }
                                    context.source.sendSuccess(
                                        { Component.literal("Added $block to allow list") }, true
                                    )
                                    1
                                })
                        ).then(
                            Commands.literal("remove")
                                .then(Commands.argument("block", IdentifierArgument.id()).suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        ServerConfigData.serverAllowList.mapNotNull { net.minecraft.resources.Identifier.tryParse(it) },
                                        builder
                                    )
                                }.executes { context ->
                                    val block = IdentifierArgument.getId(context, "block").toString()
                                    if (!ServerConfigManager.removeFromAllowList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Block $block is not in allow list")
                                        )
                                        return@executes 0
                                    }
                                    context.source.sendSuccess(
                                        { Component.literal("Removed $block from allow list") }, true
                                    )
                                    1
                                })
                        ).then(
                            Commands.literal("clear").executes { context ->
                                ServerConfigManager.clearAllowList()
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
                                ServerConfigManager.setBlockListMode(mode)
                                context.source.sendSuccess(
                                    { Component.literal("Set block list mode to $mode") }, true
                                )
                                1
                            })
                    ).then(
                        Commands.literal("reload").executes { context ->
                            ServerConfigManager.load()
                            ServerConfigManager.broadcastConfig()
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
}
