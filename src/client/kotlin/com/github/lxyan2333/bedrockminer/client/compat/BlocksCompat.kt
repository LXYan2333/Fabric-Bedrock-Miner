package com.github.lxyan2333.bedrockminer.client.compat

import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
//? if < 1.19.4 {
//import net.minecraft.core.Registry
//?} else if <= 1.20.6 {
//import net.minecraft.core.registries.BuiltInRegistries
//?} else
import fi.dy.masa.malilib.util.game.wrap.RegistryUtils.getSortedBlockList

object BlocksCompat {

    fun isValidBlockName(name: String): Boolean {
        if (IdentifierCompat.isKnownBlock(name)) {
            return true
        }
        //? if < 1.19.4 {
        //return Registry.BLOCK.any { it.name.string.equals(name, ignoreCase = true) }
        //?} else if <= 1.20.6 {
        //return BuiltInRegistries.BLOCK.any { it.name.string.equals(name, ignoreCase = true) }
        //?} else
        return getSortedBlockList().any { it.name.string.equals(name, ignoreCase = true) }
    }
}