package com.github.lxyan2333.bedrockminer.command

import com.github.lxyan2333.bedrockminer.config.ServerConfigData
import com.github.lxyan2333.bedrockminer.config.ServerConfigManager
import com.github.lxyan2333.bedrockminer.compat.CommandCompat
import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object ServerCommands {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("bedrock-miner")
                    .requires { source -> CommandCompat.requiresAdmin(source) }.then(
                        Commands.literal("blocklist").then(
                            Commands.literal("add")
                                .then(CommandCompat.idArgument("block").suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        IdentifierCompat.blockIds(), builder
                                    )
                                }.executes { context ->
                                    val block = CommandCompat.getId(context, "block")
                                    if (!ServerConfigManager.addToBlockList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Invalid block name: $block")
                                        )
                                        return@executes 0
                                    }
                                    CommandCompat.sendSuccess(context.source, Component.literal("Added $block to block list"), true)
                                    1
                                })
                        ).then(
                            Commands.literal("remove")
                                .then(CommandCompat.idArgument("block").suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        ServerConfigData.serverBlockList.mapNotNull { IdentifierCompat.parse(it) },
                                        builder
                                    )
                                }.executes { context ->
                                    val block = CommandCompat.getId(context, "block")
                                    if (!ServerConfigManager.removeFromBlockList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Block $block is not in block list")
                                        )
                                        return@executes 0
                                    }
                                    CommandCompat.sendSuccess(context.source, Component.literal("Removed $block from block list"), true)
                                    1
                                })
                        ).then(
                            Commands.literal("clear").executes { context ->
                                ServerConfigManager.clearBlockList()
                                CommandCompat.sendSuccess(context.source, Component.literal("Cleared block list"), true)
                                1
                            }).then(
                            Commands.literal("list").executes { context ->
                                val list = ServerConfigData.serverBlockList.ifEmpty { "(empty)" }
                                CommandCompat.sendSuccess(context.source, Component.literal("Block list: $list"), false)
                                1
                            })
                    ).then(
                        Commands.literal("allowlist").then(
                            Commands.literal("add")
                                .then(CommandCompat.idArgument("block").suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        IdentifierCompat.blockIds(), builder
                                    )
                                }.executes { context ->
                                    val block = CommandCompat.getId(context, "block")
                                    if (!ServerConfigManager.addToAllowList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Invalid block name: $block")
                                        )
                                        return@executes 0
                                    }
                                    CommandCompat.sendSuccess(context.source, Component.literal("Added $block to allow list"), true)
                                    1
                                })
                        ).then(
                            Commands.literal("remove")
                                .then(CommandCompat.idArgument("block").suggests { _, builder ->
                                    SharedSuggestionProvider.suggestResource(
                                        ServerConfigData.serverAllowList.mapNotNull { IdentifierCompat.parse(it) },
                                        builder
                                    )
                                }.executes { context ->
                                    val block = CommandCompat.getId(context, "block")
                                    if (!ServerConfigManager.removeFromAllowList(block)) {
                                        context.source.sendFailure(
                                            Component.literal("Block $block is not in allow list")
                                        )
                                        return@executes 0
                                    }
                                    CommandCompat.sendSuccess(context.source, Component.literal("Removed $block from allow list"), true)
                                    1
                                })
                        ).then(
                            Commands.literal("clear").executes { context ->
                                ServerConfigManager.clearAllowList()
                                CommandCompat.sendSuccess(context.source, Component.literal("Cleared allow list"), true)
                                1
                            }).then(
                            Commands.literal("list").executes { context ->
                                val list = ServerConfigData.serverAllowList.ifEmpty { "(empty)" }
                                CommandCompat.sendSuccess(context.source, Component.literal("Allow list: $list"), false)
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
                                CommandCompat.sendSuccess(context.source, Component.literal("Set block list mode to $mode"), true)
                                1
                            })
                    ).then(
                        Commands.literal("reload").executes { context ->
                            ServerConfigManager.load()
                            ServerConfigManager.broadcastConfig()
                            CommandCompat.sendSuccess(context.source, Component.literal("Reloaded server config from disk"), true)
                            1
                        }).then(
                        Commands.literal("show").executes { context ->
                            CommandCompat.sendSuccess(
                                context.source,
                                Component.literal("§7Block list:§r ${ServerConfigData.serverBlockList.ifEmpty { "(empty)" }}"),
                                false,
                            )
                            CommandCompat.sendSuccess(
                                context.source,
                                Component.literal("§7Allow list:§r ${ServerConfigData.serverAllowList.ifEmpty { "(empty)" }}"),
                                false,
                            )
                            CommandCompat.sendSuccess(
                                context.source,
                                Component.literal("§7Block list mode:§r ${ServerConfigData.serverBlockListMode}"),
                                false,
                            )
                            CommandCompat.sendSuccess(
                                context.source,
                                Component.literal("§7Protocol version:§r ${ServerConfigData.PROTOCOL_VERSION}"),
                                false,
                            )
                            1
                        })
            )
        }
    }
}
