package com.simprints.fingerprintscannermock.simulated.common

enum class SimulationSpeedBehaviour {
    INSTANT,
    REALISTIC
}

object RealisticSpeedBehaviour {

    const val DEFAULT_RESPONSE_DELAY_MS = 100L
    const val CAPTURE_FINGERPRINT_DELAY_MS = 2000L

    const val PACKET_CHUNK_SIZE_BYTES = 1024
    const val DELAY_BETWEEN_PACKETS_MS = 12L
}
