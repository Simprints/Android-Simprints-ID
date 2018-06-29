package com.simprints.id.tools.utils

import java.math.BigInteger

//TODO: move in libscanner
class ScannerUtils {
    companion object {
        private const val SERIAL_PREFIX = "SP"
        private const val MAC_ADDRESS_PREFIX = "F0:AC:D7:C"

        @JvmStatic
        fun convertAddressToSerial(macAddress: String) =
            SERIAL_PREFIX + BigInteger(macAddress
                .replace(MAC_ADDRESS_PREFIX, "")
                .replace(":", ""), 16)
    }
}
