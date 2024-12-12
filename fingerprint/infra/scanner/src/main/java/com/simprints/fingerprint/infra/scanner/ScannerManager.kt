package com.simprints.fingerprint.infra.scanner

import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerOtaOperationsWrapper
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper

/**
 * Acts as a holder for a [ScannerWrapper]. Designed to be passed around via dependency injection as
 * a singleton. The various helper methods are for accessing methods on [scanner] without the null-
 * check.
 */
interface ScannerManager {
    val scanner: ScannerWrapper
    val otaOperationsWrapper: ScannerOtaOperationsWrapper
    var currentScannerId: String?
    var currentMacAddress: String?
    val isScannerConnected: Boolean

    /**
     * Instantiates [scanner] based on currently paired MAC addresses. Does not connect to the
     * scanner.
     *
     * @throws ScannerNotPairedException if there is no valid paired MAC address corresponding to a Vero
     * @throws MultiplePossibleScannersPairedException if there are more than one paired MAC address corresponding to valid Veros
     */
    suspend fun initScanner()

    /** @throws BluetoothNotEnabledException */
    suspend fun checkBluetoothStatus()

    suspend fun deleteFirmwareFiles()
}
