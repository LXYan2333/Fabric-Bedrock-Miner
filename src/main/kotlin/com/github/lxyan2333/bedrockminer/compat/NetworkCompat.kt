package com.github.lxyan2333.bedrockminer.compat

import com.github.lxyan2333.bedrockminer.network.ModNetwork
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//?} else if >=1.20 {
/*import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
*///?} else {
/*import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
*///?}
import net.minecraft.server.level.ServerPlayer

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
    //?} else if >=1.20 {
    /*fun registerServerboundPlay(type: PacketType<*>) {
        ServerPlayNetworking.registerGlobalReceiver(type) { _, _, _ -> }
    }
    
    fun registerClientboundPlay(type: PacketType<*>) {
        // Old FabricPacket S2C channels are registered by the client receiver.
    }
    *///?} else {
    /*fun registerServerboundPlay(id: MinecraftIdentifier) {
        ServerPlayNetworking.registerGlobalReceiver(id) { _, _, _, _, _ -> }
    }

    fun registerClientboundPlay(id: MinecraftIdentifier) {
        // Raw S2C channels are registered by the client receiver.
    }
    *///?}

    fun canSendConfig(player: ServerPlayer): Boolean {
        //? if >=1.20
        return ServerPlayNetworking.canSend(player, ModNetwork.ConfigSyncPayload.TYPE)
        //? if <1.20
        //return ServerPlayNetworking.canSend(player, ModNetwork.CONFIG_SYNC_ID)
    }

    fun sendConfig(player: ServerPlayer, payload: ModNetwork.ConfigSyncPayload) {
        //? if >=1.20
        ServerPlayNetworking.send(player, payload)
        //? if <1.20
        //ServerPlayNetworking.send(player, ModNetwork.CONFIG_SYNC_ID, payload.toBuffer())
    }
}
