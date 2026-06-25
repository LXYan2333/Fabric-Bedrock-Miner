package com.github.lxyan2333.bedrockminer.compat

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.builder.RequiredArgumentBuilder
//? if >=1.19 {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
//?} else
//import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
//? if <1.19
//import net.minecraft.network.chat.TextComponent
//? if >=1.21.11 {
import net.minecraft.commands.arguments.IdentifierArgument
//?} else {
/*import net.minecraft.commands.arguments.ResourceLocationArgument
*///?}
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permissions
//?}

object CommandCompat {
    fun register(callback: (CommandDispatcher<CommandSourceStack>) -> Unit) {
        //? if >=1.19 {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> callback(dispatcher) }
        //?} else
        //CommandRegistrationCallback.EVENT.register { dispatcher, _ -> callback(dispatcher) }
    }

    fun requiresAdmin(source: CommandSourceStack): Boolean {
        //? if >=1.21.11 {
        return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN)
        //?} else
        //return source.hasPermission(4)
    }

    fun idArgument(name: String): RequiredArgumentBuilder<CommandSourceStack, MinecraftIdentifier> {
        //? if >=1.21.11 {
        return Commands.argument(name, IdentifierArgument.id())
        //?} else
        //return Commands.argument(name, ResourceLocationArgument.id())
    }

    fun getId(context: CommandContext<CommandSourceStack>, name: String): String {
        //? if >=1.21.11 {
        return IdentifierArgument.getId(context, name).toString()
        //?} else
        //return ResourceLocationArgument.getId(context, name).toString()
    }

    fun sendSuccess(source: CommandSourceStack, message: Component, broadcastToOps: Boolean) {
        //? if >=1.20 {
        source.sendSuccess({ message }, broadcastToOps)
        //?} else
        //source.sendSuccess(message, broadcastToOps)
    }

    fun literal(message: String): Component {
        //? if >=1.19
        return Component.literal(message)
        //? if <1.19
        //return TextComponent(message)
    }
}
