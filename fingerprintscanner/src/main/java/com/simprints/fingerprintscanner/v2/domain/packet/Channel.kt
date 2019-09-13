package com.simprints.fingerprintscanner.v2.domain.packet

// TODO : this channel system doesn't really make sense
sealed class Channel(val id: Int)
sealed class OutgoingChannel(id: Int) : Channel(id)
sealed class IncomingChannel(id: Int) : Channel(id)
object VeroCommand : IncomingChannel(0x10)
object VeroEvent : IncomingChannel(0x11)
object Un20Command : IncomingChannel(0x20)
object AndroidDevice : OutgoingChannel(0xA0)
