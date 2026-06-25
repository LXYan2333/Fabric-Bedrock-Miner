package com.github.lxyan2333.bedrockminer.compat

//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries
//?} else
//import net.minecraft.core.Registry
//? if >=1.21.11 {
import net.minecraft.resources.Identifier
//?} else {
/*import net.minecraft.resources.ResourceLocation
*///?}
import net.minecraft.world.level.block.Block

//? if >=1.21.11 {
typealias MinecraftIdentifier = Identifier
//?} else
//typealias MinecraftIdentifier = ResourceLocation

object IdentifierCompat {
    fun of(namespace: String, path: String): MinecraftIdentifier {
        //? if >=1.21.11 {
        return Identifier.fromNamespaceAndPath(namespace, path)
        //?} else if >=1.21 {
        //return ResourceLocation.fromNamespaceAndPath(namespace, path)
        //?} else
        //return ResourceLocation(namespace, path)
    }

    fun parse(id: String): MinecraftIdentifier? {
        //? if >=1.21.11 {
        return Identifier.tryParse(id)
        //?} else
        //return ResourceLocation.tryParse(id)
    }

    fun isKnownBlock(id: String): Boolean {
        val key = parse(id) ?: return false
        return blockRegistry().containsKey(key)
    }

    fun block(id: MinecraftIdentifier): Block {
        //? if >=1.21.11 {
        return blockRegistry().getValue(id)
        //?} else
        //return blockRegistry().get(id)
    }

    fun block(id: String): Block? {
        val key = parse(id) ?: return null
        if (!blockRegistry().containsKey(key)) return null
        return block(key)
    }

    fun blockIds(): Set<MinecraftIdentifier> {
        return blockRegistry().keySet()
    }

    fun blockId(block: Block): MinecraftIdentifier {
        return blockRegistry().getKey(block)
    }

    private fun blockRegistry() =
        //? if >=1.19.3
        BuiltInRegistries.BLOCK
        //? if <1.19.3
        //Registry.BLOCK
}
