package com.simprints.fingerprint.infra.scanner.v1

import android.util.Log
import java.util.regex.Pattern

object ScannerUtils {
    private val SCANNER_ADDR = Pattern.compile("F0:AC:D7:C\\p{XDigit}:\\p{XDigit}{2}:\\p{XDigit}{2}")

    /**
     * Checks that a MAC address is a Simprint's scanner address
     *
     * @param macAddress The MAC address to check
     * @return True if and only if the specified MAC address is a valid scanner address
     */
    @JvmStatic
    fun isScannerAddress(macAddress: String): Boolean {
        val result = SCANNER_ADDR.matcher(macAddress).matches()
        log(String.format("isScannerAddress(%s) -> %s", macAddress, result))
        return result
    }

    @JvmStatic
    fun log(s: String) {
        Log.d("fingerprintscanner", s)
    }
}
