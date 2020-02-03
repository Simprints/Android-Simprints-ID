package com.simprints.fingerprintscanner.v2.scanner.ota.stm

data class FirmwareByteChunk(val address: ByteArray,
                             val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FirmwareByteChunk

        if (!address.contentEquals(other.address)) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.contentHashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
