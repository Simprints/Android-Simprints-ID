package com.simprints.libscanner

import android.util.Log
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import java.math.BigInteger
import java.util.regex.Pattern

object ScannerUtils {

        private val SCANNER_ADDR = Pattern.compile("F0:AC:D7:C\\p{XDigit}:\\p{XDigit}{2}:\\p{XDigit}{2}")
        private val MAC_ADDR = Pattern.compile("\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}")
        private const val SERIAL_PREFIX = "SP"
        private const val MAC_ADDRESS_PREFIX = "F0:AC:D7:C"
        private const val MAC_BLOCK_SIZE = 1048576
        private const val SERIAL_BLOCK_SIZE = 999999

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

        /**
         * Checks that a string is a valid MAC address
         *
         * @param macAddress The string to check
         * @return True if and only if the specified string is MAC address
         * formatted as XX:XX:XX:XX:XX:XX where X is an hexadecimal digit
         */
        @JvmStatic
        fun isMACAddress(macAddress: String): Boolean {
            val result = MAC_ADDR.matcher(macAddress).matches()
            log(String.format("isMACAddress(%s) -> %s", macAddress, result))
            return result
        }

        @JvmStatic
        fun convertAddressToSerial(macAddress: String): String {
            return SERIAL_PREFIX + BigInteger(macAddress
                    .replace(MAC_ADDRESS_PREFIX, "")
                    .replace(":", ""), 16)
        }

        /**
         * READ THIS!
         * Because our MAC address range (0:00:00 -> F:FF:FF) is larger than the serial number
         * range SP(000000 -> 999999) we end up with duplicate serial numbers. This means that a
         * serial number <= 48575 could have two potential MAC addresses.
         * Example: SP48575 could be 0:BD:BF or F:FF:FF.
         * Because of this we return a list of potential MAC addresses.
         *
         * @param serial The scanner serial number
         * @return List of potential MAC addresses
         */
        @JvmStatic
        fun convertSerialToPotentialAddress(serial: String): List<String> {
            val addresses = arrayListOf<String>()

            serial.removePrefix(SERIAL_PREFIX).toInt().let {
                addresses.add(serialHexToMacAddress(getMacHexFromInt(it)))

                if (it < (MAC_BLOCK_SIZE - SERIAL_BLOCK_SIZE))
                    addresses.add(serialHexToMacAddress(getMacHexFromInt(it + SERIAL_BLOCK_SIZE)))
            }

            return addresses
        }

        private fun getMacHexFromInt(int: Int): String = Integer.toHexString(int)
                .toUpperCase().padStart(5, '0')

        private fun serialHexToMacAddress(hex: String): String = MAC_ADDRESS_PREFIX +
                StringBuilder(hex).insert(1, ":").insert(4, ":").toString()

        /**
         * Checks which ones of the paired bluetooth devices are Simprint's scanners
         *
         * @return a list of paired Simprint's scanners
         */
        @JvmStatic
        fun getPairedScanners(bluetoothAdapter: BluetoothComponentAdapter): List<String> {
            val pairedScanners = arrayListOf<String>()

            for (device in bluetoothAdapter.getBondedDevices()) {
                if (isScannerAddress(device.address))
                    pairedScanners.add(device.address)
            }

            return pairedScanners.toList()
        }

        @JvmStatic
        fun log(s: String) {
            Log.d("libscanner", s)
        }

}
