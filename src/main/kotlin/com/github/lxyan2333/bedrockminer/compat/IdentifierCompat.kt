package com.github.lxyan2333.bedrockminer.compat

import net.minecraft.core.registries.BuiltInRegistries
//? if >=1.21.11 {
import net.minecraft.resources.Identifier
//?} else {
//import net.minecraft.resources.ResourceLocation
//?}
import net.minecraft.world.level.block.Block

//? if >=1.21.11 {
typealias MinecraftIdentifier = Identifier
//?} else
//typealias MinecraftIdentifier = ResourceLocation

object IdentifierCompat {
    fun of(namespace: String, path: String): MinecraftIdentifier {
        //? if >=1.21.11 {
        return Identifier.fromNamespaceAndPath(namespace, path)
        //?} else
        //return ResourceLocation.fromNamespaceAndPath(namespace, path)
    }

    fun parse(id: String): MinecraftIdentifier? {
        //? if >=1.21.11 {
        return Identifier.tryParse(id)
        //?} else
        //return ResourceLocation.tryParse(id)
    }

    fun isKnownBlock(id: String): Boolean {
        val key = parse(id) ?: return false
        return BuiltInRegistries.BLOCK.containsKey(key)
    }

    fun block(id: MinecraftIdentifier): Block {
        //? if >=1.21.11 {
        return BuiltInRegistries.BLOCK.getValue(id)
        //?} else
        //return BuiltInRegistries.BLOCK.get(id)
    }

    fun block(id: String): Block? {
        val key = parse(id) ?: return null
        if (!BuiltInRegistries.BLOCK.containsKey(key)) return null
        return block(key)
    }
}
