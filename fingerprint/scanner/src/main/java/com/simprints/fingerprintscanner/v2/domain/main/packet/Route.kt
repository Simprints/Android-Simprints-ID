package com.simprints.fingerprintscanner.v2.domain.main.packet

sealed class Route(val id: RouteId) {

    sealed class Local(id: RouteId) : Route(id) {
        object AndroidDevice : Local(RouteId.ANDROID_DEVICE)
    }

    sealed class Remote(id: RouteId) : Route(id) {
        object VeroServer : Remote(RouteId.VERO_SERVER)
        object VeroEvent : Remote(RouteId.VERO_EVENT)
        object Un20Server : Remote(RouteId.UN20_SERVER)
    }
}
