package com.simprints.fingerprint.infra.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import javax.inject.Inject

/**
 * Helper class for handling MAC addresses and pairing programmatically.
 */
class ScannerPairingManager @Inject internal constructor(
    private val bluetoothAdapter: ComponentBluetoothAdapter,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val scannerGenerationDeterminer: ScannerGenerationDeterminer,
    private val serialNumberConverter: SerialNumberConverter,
    private val configRepository: ConfigRepository,
) {
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

    fun startPairingToDevice(address: String) {
        val device = bluetoothAdapter.getRemoteDevice(address)
        device.createBond()
    }

    /**
     * @throws ScannerNotPairedException
     * @throws MultiplePossibleScannersPairedException
     */
    suspend fun getPairedScannerAddressToUse(): String {
        val validPairedScanners =
            getPairedScannerAddresses().filter { isScannerGenerationValidForProject(it) }
        return when (validPairedScanners.size) {
            0 -> throw ScannerNotPairedException()
            1 -> validPairedScanners.first()
            else -> deduceScannerFromLastScannerUsed(validPairedScanners)
        }
    }

    private suspend fun isScannerGenerationValidForProject(address: String): Boolean =
        configRepository.getProjectConfiguration().fingerprint?.allowedScanners?.contains(
            scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber(
                serialNumberConverter.convertMacAddressToSerialNumber(address),
            ),
        ) ?: false

    private suspend fun deduceScannerFromLastScannerUsed(pairedScanners: List<String>): String {
        val lastSerialNumberUsed = recentUserActivityManager.getRecentUserActivity().lastScannerUsed
        if (isScannerSerialNumber(lastSerialNumberUsed)) {
            val lastAddressUsed =
                serialNumberConverter.convertSerialNumberToMacAddress(lastSerialNumberUsed)
            if (pairedScanners.contains(lastAddressUsed)) {
                return lastAddressUsed
            } else {
                throw MultiplePossibleScannersPairedException()
            }
        } else {
            throw MultiplePossibleScannersPairedException()
        }
    }

    private fun getPairedScannerAddresses(): List<String> = bluetoothAdapter
        .getBondedDevices()
        .map { it.address }
        .filter { isScannerAddress(it) }

    fun isAddressPaired(address: String): Boolean = getPairedScannerAddresses().contains(address)

    fun isScannerAddress(macAddress: String): Boolean = SCANNER_ADDRESS_REGEX.matches(macAddress)

    private fun isScannerSerialNumber(serialNumber: String): Boolean = SERIAL_NUMBER_REGEX.matches(serialNumber)

    fun bluetoothPairStateChangeReceiver(
        onPairSuccess: () -> Unit,
        onPairFailed: (Boolean) -> Unit,
    ): BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent,
        ) {
            if (intent.action == ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val bondState = intent.getIntExtra(
                    ComponentBluetoothDevice.EXTRA_BOND_STATE,
                    ComponentBluetoothDevice.BOND_NONE,
                )
                val failReason = intent.getIntExtra(
                    ComponentBluetoothDevice.EXTRA_REASON,
                    ComponentBluetoothDevice.BOND_SUCCESS,
                )

                val pairSucceeded = bondState == ComponentBluetoothDevice.BOND_BONDED
                val pairingFailed = bondState == ComponentBluetoothDevice.BOND_NONE &&
                    failReason != ComponentBluetoothDevice.BOND_SUCCESS &&
                    failReason != ComponentBluetoothDevice.UNBOND_REASON_REMOVED
                if (pairSucceeded) {
                    onPairSuccess()
                } else if (pairingFailed) {
                    val pairingRejected = failReason == ComponentBluetoothDevice.REASON_AUTH_FAILED ||
                        failReason == ComponentBluetoothDevice.REASON_AUTH_REJECTED ||
                        failReason == ComponentBluetoothDevice.REASON_AUTH_CANCELED ||
                        failReason == ComponentBluetoothDevice.REASON_REMOTE_AUTH_CANCELED

                    onPairFailed(pairingRejected)
                }
            }
        }
    }

    companion object {
        private const val MAC_ADDRESS_PREFIX = "F0:AC:D7:C"
        private val SCANNER_ADDRESS_REGEX =
            Regex("""$MAC_ADDRESS_PREFIX[0-9A-F]:[0-9A-F]{2}:[0-9A-F]{2}""")
        private const val SERIAL_PREFIX = "SP"
        private val SERIAL_NUMBER_REGEX = Regex("""^$SERIAL_PREFIX[0-9]{6}$""")

        private val IS_DIGITS_ONLY_REGEX = Regex("""^[0-9]+$""")
    }
}
