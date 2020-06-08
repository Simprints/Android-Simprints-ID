package com.simprints.fingerprint.scanner.pairing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import java.util.regex.Pattern

class ScannerPairingManager(private val bluetoothAdapter: ComponentBluetoothAdapter) {

    /**
     * Turns user entered text into a valid serial number, e.g. "003456" -> "SP003456"
     * @throws NumberFormatException if the text does not contain an appropriate number
     */
    fun interpretEnteredTextAsSerialNumber(text: String): String {
        if (text.length != 6) throw NumberFormatException("Incorrect number of digits entered for serial number: $text")
        if (!IS_DIGITS_ONLY_REGEX.matches(text)) throw NumberFormatException("Non-digits found in serial number: $text")
        val number = text.toInt()
        if (number < 0 || number > 999999) throw NumberFormatException("Number out of range for serial number")
        return SERIAL_PREFIX + number.toString(10).padStart(6, '0')
    }

    /**
     * Un-pairs all other devices that follow Simprints' MAC address format, and begins pairing to
     * the given address
     */
    fun pairOnlyToDevice(address: String) {
        val device = bluetoothAdapter.getRemoteDevice(address)

        bluetoothAdapter.getBondedDevices().forEach {
            if (isScannerAddress(it.address) && it.address != device.address) {
                it.removeBond()
            }
        }

        device.createBond()
    }

    fun getPairedScannerAddresses(): List<String> =
        bluetoothAdapter
            .getBondedDevices()
            .map { it.address }
            .filter { isScannerAddress(it) }

    fun isOnlyPairedToOneScanner(): Boolean =
        getPairedScannerAddresses().count() == 1

    fun isScannerAddress(macAddress: String): Boolean =
        SCANNER_ADDRESS_REGEX.matcher(macAddress).matches()

    fun bluetoothPairStateChangeReceiver(onPairSuccess: () -> Unit, onPairFailed: () -> Unit): BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action == ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val bondState = intent.getIntExtra(ComponentBluetoothDevice.EXTRA_BOND_STATE, ComponentBluetoothDevice.BOND_NONE)
                    val failReason = intent.getIntExtra(ComponentBluetoothDevice.EXTRA_REASON, ComponentBluetoothDevice.BOND_SUCCESS)
                    val pairSucceeded = bondState == ComponentBluetoothDevice.BOND_BONDED
                    val pairingFailed = bondState == ComponentBluetoothDevice.BOND_NONE
                        && failReason != ComponentBluetoothDevice.BOND_SUCCESS
                        && failReason != ComponentBluetoothDevice.UNBOND_REASON_REMOVED
                    if (pairSucceeded) {
                        onPairSuccess()
                    } else if (pairingFailed) {
                        onPairFailed()
                    }
                }
            }
        }

    companion object {
        private const val MAC_ADDRESS_PREFIX = "F0:AC:D7:C"
        private val SCANNER_ADDRESS_REGEX = Pattern.compile("$MAC_ADDRESS_PREFIX\\p{XDigit}:\\p{XDigit}{2}:\\p{XDigit}{2}")
        private const val SERIAL_PREFIX = "SP"

        private val IS_DIGITS_ONLY_REGEX = Regex("""^[0-9]+$""")
    }
}
