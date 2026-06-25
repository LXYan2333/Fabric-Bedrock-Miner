package com.github.lxyan2333.bedrockminer.network

import com.github.lxyan2333.bedrockminer.compat.NetworkCompat
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

object ModNetwork {
    val CONFIG_SYNC_ID = Identifier.fromNamespaceAndPath("bedrock-miner", "config_sync")
    val DUMMY_ID = Identifier.fromNamespaceAndPath("bedrock-miner", "dummy")

    data class ConfigSyncPayload(
        val protocolVersion: Int,
        val blockList: Set<String>,
        val allowList: Set<String>,
        val blockListMode: String,
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<ConfigSyncPayload> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<ConfigSyncPayload>(CONFIG_SYNC_ID)

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> =
                StreamCodec.of({ buffer, payload ->
                    buffer.writeVarInt(payload.protocolVersion)

                    buffer.writeVarInt(payload.blockList.size)
                    payload.blockList.forEach { buffer.writeUtf(it) }

                    buffer.writeVarInt(payload.allowList.size)
                    payload.allowList.forEach { buffer.writeUtf(it) }

                    buffer.writeUtf(payload.blockListMode)
                }, { buffer ->
                    val protocolVersion = buffer.readVarInt()

                    val blockList = mutableSetOf<String>()
                    val blockListSize = buffer.readVarInt()
                    repeat(blockListSize) { blockList.add(buffer.readUtf()) }

                    val allowList = mutableSetOf<String>()
                    val allowListSize = buffer.readVarInt()
                    repeat(allowListSize) { allowList.add(buffer.readUtf()) }

                    val blockListMode = buffer.readUtf()
                    ConfigSyncPayload(protocolVersion, blockList, allowList, blockListMode)
                })
        }
    }

    object DummyPayload : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<DummyPayload> = TYPE

        val TYPE = CustomPacketPayload.Type<DummyPayload>(DUMMY_ID)

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, DummyPayload> =
            StreamCodec.of({ _, _ -> }, { _ -> DummyPayload })
    }

    fun registerPayloadTypes() {
        NetworkCompat.registerServerboundPlay(DummyPayload.TYPE, DummyPayload.STREAM_CODEC)
        NetworkCompat.registerClientboundPlay(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC)
    }
}
