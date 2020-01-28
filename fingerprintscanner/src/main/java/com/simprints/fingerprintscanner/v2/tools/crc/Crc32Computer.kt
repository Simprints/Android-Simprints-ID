package com.simprints.fingerprintscanner.v2.tools.crc

class Crc32Computer {

    fun computeCrc32(bytes: ByteArray, crc32Table: IntArray): Int {

        var crcValue = -0x1
        for (b in bytes) {
            crcValue = crcValue ushr 8 xor crc32Table[crcValue xor b.toInt() and 0xff]
        }
        crcValue = crcValue xor -0x1

        return crcValue
    }
}
