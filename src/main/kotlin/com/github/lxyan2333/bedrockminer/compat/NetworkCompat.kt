package com.github.lxyan2333.bedrockminer.compat

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

object NetworkCompat {
    fun <T : CustomPacketPayload> registerServerboundPlay(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<RegistryFriendlyByteBuf, T>,
    ) {
        //? if >=26.1 {
        PayloadTypeRegistry.serverboundPlay().register(type, codec)
        //?} else
        //PayloadTypeRegistry.playC2S().register(type, codec)
    }

    fun <T : CustomPacketPayload> registerClientboundPlay(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<RegistryFriendlyByteBuf, T>,
    ) {
        //? if >=26.1 {
        PayloadTypeRegistry.clientboundPlay().register(type, codec)
        //?} else
        //PayloadTypeRegistry.playS2C().register(type, codec)
    }
}
