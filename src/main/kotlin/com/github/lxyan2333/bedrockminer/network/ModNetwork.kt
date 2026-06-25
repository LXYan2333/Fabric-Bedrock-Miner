package com.github.lxyan2333.bedrockminer.network

import com.github.lxyan2333.bedrockminer.compat.IdentifierCompat
import com.github.lxyan2333.bedrockminer.compat.NetworkCompat
//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//?} else {
//import net.fabricmc.fabric.api.networking.v1.FabricPacket
//import net.fabricmc.fabric.api.networking.v1.PacketType
//import net.minecraft.network.FriendlyByteBuf
//?}

//? if >=1.20.5 {
typealias ModPacketBuf = RegistryFriendlyByteBuf
//?} else
//typealias ModPacketBuf = FriendlyByteBuf

object ModNetwork {
    val CONFIG_SYNC_ID = IdentifierCompat.of("bedrock-miner", "config_sync")
    val DUMMY_ID = IdentifierCompat.of("bedrock-miner", "dummy")

    data class ConfigSyncPayload(
        val protocolVersion: Int,
        val blockList: Set<String>,
        val allowList: Set<String>,
        val blockListMode: String,
    )
    //? if >=1.20.5
    : CustomPacketPayload
    //? if <1.20.5
    //: FabricPacket
    {
        //? if <1.20.5 {
        /*constructor(buf: FriendlyByteBuf) : this(
            buf.readVarInt(),
            readStringSet(buf),
            readStringSet(buf),
            buf.readUtf(),
        )

        override fun write(buf: FriendlyByteBuf) {
            writeToBuffer(buf)
        }

        override fun getType(): PacketType<*> = TYPE
        *///?}

        //? if >=1.20.5 {
        override fun type(): CustomPacketPayload.Type<ConfigSyncPayload> = TYPE
        //?}

        companion object {
            //? if >=1.20.5 {
            val TYPE = CustomPacketPayload.Type<ConfigSyncPayload>(CONFIG_SYNC_ID)

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> =
                StreamCodec.of({ buffer, payload ->
                    payload.writeToBuffer(buffer)
                }, { buffer ->
                    ConfigSyncPayload(
                        buffer.readVarInt(),
                        readStringSet(buffer),
                        readStringSet(buffer),
                        buffer.readUtf(),
                    )
                })
            //?} else
            //val TYPE: PacketType<ConfigSyncPayload> = PacketType.create(CONFIG_SYNC_ID, ::ConfigSyncPayload)

            private fun readStringSet(buffer: ModPacketBuf): Set<String> {
                val list = mutableSetOf<String>()
                val size = buffer.readVarInt()
                repeat(size) { list.add(buffer.readUtf()) }
                return list
            }
        }

        private fun writeToBuffer(buffer: ModPacketBuf) {
            buffer.writeVarInt(protocolVersion)
            writeStringSet(buffer, blockList)
            writeStringSet(buffer, allowList)
            buffer.writeUtf(blockListMode)
        }

        private fun writeStringSet(buffer: ModPacketBuf, values: Set<String>) {
            buffer.writeVarInt(values.size)
            values.forEach { buffer.writeUtf(it) }
        }
    }

    object DummyPayload
    //? if >=1.20.5
    : CustomPacketPayload
    //? if <1.20.5
    //: FabricPacket
    {
        //? if >=1.20.5 {
        override fun type(): CustomPacketPayload.Type<DummyPayload> = TYPE
        //?} else {
        /*override fun write(buf: FriendlyByteBuf) {
        }

        override fun getType(): PacketType<*> = TYPE
        *///?}

        //? if >=1.20.5 {
        val TYPE = CustomPacketPayload.Type<DummyPayload>(DUMMY_ID)

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, DummyPayload> =
            StreamCodec.of({ _, _ -> }, { _ -> DummyPayload })
        //?} else
        //val TYPE: PacketType<DummyPayload> = PacketType.create(DUMMY_ID) { DummyPayload }
    }

    fun registerPayloadTypes() {
        //? if >=1.20.5 {
        NetworkCompat.registerServerboundPlay(DummyPayload.TYPE, DummyPayload.STREAM_CODEC)
        NetworkCompat.registerClientboundPlay(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC)
        //?} else {
        //NetworkCompat.registerServerboundPlay(DummyPayload.TYPE)
        //NetworkCompat.registerClientboundPlay(ConfigSyncPayload.TYPE)
        //?}
    }
}
