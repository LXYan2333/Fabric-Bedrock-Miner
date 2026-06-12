package com.github.lxyan2333.bedrockminer.client.config

import fi.dy.masa.malilib.config.IConfigOptionListEntry
import fi.dy.masa.malilib.util.StringUtils

enum class AllowOrBlockMode(
    private val translationKey: String,
) : IConfigOptionListEntry {
    BLOCKED("bedrockminer.config.alloworblockmode.blocked"), ALLOWED("bedrockminer.config.alloworblockmode.allowed"), ;

    override fun getStringValue(): String = this.name

    override fun getDisplayName(): String = StringUtils.translate(translationKey)

    override fun cycle(moveForward: Boolean): IConfigOptionListEntry {
        val values = entries
        val next = (values.indexOf(this) + if (moveForward) 1 else -1).mod(values.size)
        return values[next]
    }

    override fun fromString(value: String): IConfigOptionListEntry = entries.firstOrNull { it.name == value } ?: BLOCKED
}
