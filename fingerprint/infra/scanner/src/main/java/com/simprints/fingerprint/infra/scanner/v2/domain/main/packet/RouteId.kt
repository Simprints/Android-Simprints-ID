package com.simprints.fingerprint.infra.scanner.v2.domain.main.packet

enum class RouteId(
    val value: Byte,
) {
    VERO_SERVER(0x10),
    VERO_EVENT(0x11),
    UN20_SERVER(0x20),
    ANDROID_DEVICE(0xA0.toByte()),
}
