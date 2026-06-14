package com.github.lxyan2333.bedrockminer.config

object ServerConfigData {
    const val PROTOCOL_VERSION = 1

    val SPECIAL_BLOCKS = setOf(
        "minecraft:barrier",
        "minecraft:command_block",
        "minecraft:chain_command_block",
        "minecraft:repeating_command_block",
        "minecraft:structure_block",
        "minecraft:structure_void",
        "minecraft:jigsaw",
    )

    var serverHasMod = false

    var serverProtocolVersion = -1

    var serverBlockList: MutableSet<String> = SPECIAL_BLOCKS.toMutableSet()

    var serverAllowList: MutableSet<String> = mutableSetOf()

    var serverBlockListMode: String = "ALLOWED"
}
