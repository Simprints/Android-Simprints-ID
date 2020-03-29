package com.simprints.fingerprint.scanner

import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString
import java.util.regex.Pattern

class ScannerPairingManager(private val bluetoothAdapter: ComponentBluetoothAdapter) {

    /**
     * Expect an NFC tag with type application/vnd.bluetooth.ep.oob
     * The scanner address is found 15 * 4 = 60 bytes into the payload of the loaded data
     * The input of this function should look like mifare.readPages(15)
     * The address will appear reversed
     */
    fun interpretNfcDataAsScannerMacAddress(payload: ByteArray): String =
        payload.sliceArray(0..5).reversedArray().toHexString().replace(" ", ":").also {
            if (!isScannerAddress(it)) throw IllegalArgumentException("NFC chip is does not contain a valid Simprints scanner MAC address")
        }

    /**
     * Un-pairs all other devices that follow Simprints' MAC address format
     * @return True if pairing is about to begin, False could not start pairing
     */
    fun pairOnlyToDevice(address: String): Boolean {
        val device = bluetoothAdapter.getRemoteDevice(address)

        bluetoothAdapter.getBondedDevices().forEach {
            if (isScannerAddress(it.address)) {
                it.removeBond()
            }
        }

        return device.createBond()
    }

    fun getPairedScannerAddresses(): List<String> =
        bluetoothAdapter
            .getBondedDevices()
            .map { it.address }
            .filter { isScannerAddress(it) }

    fun isOnlyPairedToOneScanner(): Boolean =
        getPairedScannerAddresses().count() == 1

    fun convertAddressToSerialNumber(address: String): String =
        SERIAL_PREFIX + address.replace(":", "").substring(7..11)
            .toInt(16).toString(10).padStart(6, '0')

    private fun isScannerAddress(macAddress: String): Boolean =
        SCANNER_ADDRESS_REGEX.matcher(macAddress).matches()

    companion object {
        private const val MAC_ADDRESS_PREFIX = "F0:AC:D7:C"
        private val SCANNER_ADDRESS_REGEX = Pattern.compile("$MAC_ADDRESS_PREFIX\\p{XDigit}:\\p{XDigit}{2}:\\p{XDigit}{2}")
        private const val SERIAL_PREFIX = "SP"
    }
}
