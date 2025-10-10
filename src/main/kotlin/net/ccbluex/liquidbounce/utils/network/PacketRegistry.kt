package net.ccbluex.liquidbounce.utils.network

import net.ccbluex.liquidbounce.utils.kotlin.enumMap
import net.minecraft.network.NetworkSide
import net.minecraft.util.Identifier

/**
 * A registry for packet types, allowing registration of packet identifiers
 * for both clientbound and serverbound packets.
 * This is used to keep track of which packets are registered for each side of the network.
 *
 * Be aware that serverbound means packets sent from the client to the server (C2S),
 * and clientbound means packets sent from the server to the client (S2C).
 */
val packetRegistry = enumMap<NetworkSide, MutableSet<Identifier>>()
