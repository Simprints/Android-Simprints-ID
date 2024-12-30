package com.simprints.fingerprint.infra.scanner.tools

import android.annotation.SuppressLint
import javax.inject.Inject

/**
 * Helper class for converting to and from Vero serial numbers and MAC addresses.
 */
class SerialNumberConverter @Inject constructor() {
    /**
     * @param address the MAC address in "F0:AC:D7:CX:XX:XX" format
     * @return the serial number in "SPXXXXXX" format
     */
    fun convertMacAddressToSerialNumber(address: String): String = SERIAL_PREFIX + address
        .replace(":", "")
        .substring(7..11)
        .toInt(16)
        .rem(SERIAL_NUMBER_LIMIT)
        .toString(10)
        .padStart(6, '0')

    /**
     * @param serialNumber the serial number in "SPXXXXXX" format
     * @return the MAC address in "F0:AC:D7:CX:XX:XX" format
     */
    fun convertSerialNumberToMacAddress(serialNumber: String): String = serialNumber.removePrefix(SERIAL_PREFIX).toInt().let { serialInt ->
        val macInt = if (VERO_1_WITH_MAC_ADDRESS_ABOVE_SERIAL_NUMBER_LIMIT.contains(serialNumber)) {
            serialInt + SERIAL_NUMBER_LIMIT
        } else {
            serialInt
        }
        serialHexToMacAddress(getMacHexFromInt(macInt))
    }

    @SuppressLint("DefaultLocale")
    private fun getMacHexFromInt(int: Int): String = Integer
        .toHexString(int)
        .uppercase()
        .padStart(5, '0')

    private fun serialHexToMacAddress(hex: String): String = MAC_ADDRESS_PREFIX +
        StringBuilder(hex).insert(1, ":").insert(4, ":").toString()

    companion object {
        const val MAC_ADDRESS_PREFIX = "F0:AC:D7:C"
        const val SERIAL_PREFIX = "SP"

        const val SERIAL_NUMBER_LIMIT = 1000000

        // This is the list of serial numbers to Vero 1s that have exceptional MAC addresses
        // with decimal value above the serial number limit. Taken from the old manufacturing db.
        val VERO_1_WITH_MAC_ADDRESS_ABOVE_SERIAL_NUMBER_LIMIT = listOf(
            "SP018257",
            "SP009113",
            "SP047961",
            "SP023967",
            "SP000652",
        )
    }
}
