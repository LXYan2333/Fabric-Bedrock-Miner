package com.github.lxyan2333.bedrockminer.client.config

import fi.dy.masa.malilib.config.IConfigOptionListEntry
import fi.dy.masa.malilib.util.StringUtils

enum class ApproachMode(
    private val translationKey: String,
) : IConfigOptionListEntry {
    VANILLA_FAST("bedrockminer.config.approach.vanilla_fast"),
    CARPET_ACCURATE("bedrockminer.config.approach.carpet_accurate"),
    VANILLA_ALL_DIRECTION("bedrockminer.config.approach.vanilla_all_direction"),
    ;

    override fun getStringValue(): String = this.name

    override fun getDisplayName(): String = StringUtils.translate(translationKey)

    override fun cycle(moveForward: Boolean): IConfigOptionListEntry {
        val values = entries
        val next = (values.indexOf(this) + if (moveForward) 1 else -1).mod(values.size)
        return values[next]
    }

    override fun fromString(value: String): IConfigOptionListEntry =
        entries.firstOrNull { it.name == value } ?: VANILLA_FAST
}