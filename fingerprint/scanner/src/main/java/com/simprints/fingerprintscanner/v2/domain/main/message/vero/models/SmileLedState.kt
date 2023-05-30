package com.simprints.fingerprintscanner.v2.domain.main.message.vero.models

data class SmileLedState(
    val led1: LedState,
    val led2: LedState,
    val led3: LedState,
    val led4: LedState,
    val led5: LedState
) {

    fun getBytes() =
        led1.getBytes() + led2.getBytes() + led3.getBytes() + led4.getBytes() + led5.getBytes()

    companion object {
        fun fromBytes(bytes: ByteArray) =
            SmileLedState(
                LedState.fromBytes(bytes.sliceArray(0..3)),
                LedState.fromBytes(bytes.sliceArray(4..7)),
                LedState.fromBytes(bytes.sliceArray(8..11)),
                LedState.fromBytes(bytes.sliceArray(12..15)),
                LedState.fromBytes(bytes.sliceArray(16..19))
            )
    }
}
