package com.github.lxyan2333.bedrockminer.compat

//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//?} else {
//import net.fabricmc.fabric.api.networking.v1.PacketType
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
//?}

object NetworkCompat {
    //? if >=1.20.5 {
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
    //?} else {
    //fun registerServerboundPlay(type: PacketType<*>) {
    //    ServerPlayNetworking.registerGlobalReceiver(type) { _, _, _ -> }
    //}
    //
    //fun registerClientboundPlay(type: PacketType<*>) {
    //    // Old FabricPacket S2C channels are registered by the client receiver.
    //}
    //?}
}
