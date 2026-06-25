package com.github.lxyan2333.bedrockminer.compat

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
//? if >=1.21.11 {
import net.minecraft.commands.arguments.IdentifierArgument
//?} else {
//import net.minecraft.commands.arguments.ResourceLocationArgument
//?}
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permissions
//?}

object CommandCompat {
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
}
