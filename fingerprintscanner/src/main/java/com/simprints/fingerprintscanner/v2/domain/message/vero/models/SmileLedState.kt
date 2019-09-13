package com.simprints.fingerprintscanner.v2.domain.message.vero.models

data class SmileLedState(
    val led1: LedState,
    val led2: LedState,
    val led3: LedState,
    val led4: LedState,
    val led5: LedState
) {

    fun getBytes() =
        led1.getBytes() + led2.getBytes() + led3.getBytes() + led4.getBytes() + led5.getBytes()
}
