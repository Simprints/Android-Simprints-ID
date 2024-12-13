package com.simprints.fingerprint.infra.scanner.v2.scanner

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class to manage and provide access to scanner-related information.
 *
 * This class handles two key pieces of information:
 * 1. `scannerId`: The unique identifier of the scanner, available after connecting to the scanner.
 * 2. `un20SerialNumber`: The serial number of the scanner, available only after reading an unprocessed image.
 *
 * Both properties can be updated independently based on the scanner's lifecycle and operations.
 *
 */
@Singleton
class ScannerInfo @Inject constructor() {
    /**
     * The unique identifier of the scanner.
     * This property is set after successfully connecting to the scanner.
     */
    var scannerId: String? = null
        private set

    /**
     * The serial number of the scanner.
     * This property is set after successfully reading an unprocessed image from the scanner.
     */
    var un20SerialNumber: String? = null
        private set

    /**
     * Updates the scanner ID.
     *
     * @param id The unique identifier of the scanner.
     */
    fun setScannerId(id: String) {
        scannerId = id
    }

    /**
     * Updates the serial number of the scanner.
     *
     * @param serialNumber The serial number retrieved from an unprocessed image.
     */
    fun setUn20SerialNumber(serialNumber: String) {
        un20SerialNumber = serialNumber
    }

    /**
     *  Clears the scanner ID and serial number.
     */
    fun clear() {
        scannerId = null
        un20SerialNumber = null
    }
}
