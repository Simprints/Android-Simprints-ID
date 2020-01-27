package com.simprints.fingerprintscanner.v2.domain.main.packet

sealed class Channel(val id: ChannelId) {

    sealed class Local(id: ChannelId) : Channel(id) {
        object AndroidDevice : Local(ChannelId.ANDROID_DEVICE)
    }

    sealed class Remote(id: ChannelId) : Channel(id) {
        object VeroServer : Remote(ChannelId.VERO_SERVER)
        object VeroEvent : Remote(ChannelId.VERO_EVENT)
        object Un20Server : Remote(ChannelId.UN20_SERVER)
    }
}
