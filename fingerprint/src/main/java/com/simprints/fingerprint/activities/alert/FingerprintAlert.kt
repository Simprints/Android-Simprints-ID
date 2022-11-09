package com.simprints.fingerprint.activities.alert

/**
 * This enum class represents the different causes or types of Fingerprint alerts.
 *
 * - [BLUETOOTH_NOT_SUPPORTED]  an alert indicating that the device doesn't support bluetooth
 * - [BLUETOOTH_NOT_ENABLED]  an alert indicating that the bluetooth isn't enabled
 * - [NOT_PAIRED]  an alert indicating the device isn't paired with the scanner
 * - [MULTIPLE_PAIRED_SCANNERS]  an alert indicating multiple scanners are paired to the device
 * - [DISCONNECTED]  an alert indicating a scanner is disconnected, usually duringa capture process
 * - [LOW_BATTERY]  an alert indicating that the scanner has a low batter
 * - [UNEXPECTED_ERROR]  an unexpected error occurred while processing fingerprint request
 */
enum class FingerprintAlert {

    BLUETOOTH_NOT_SUPPORTED,
    BLUETOOTH_NOT_ENABLED,
    NOT_PAIRED,
    MULTIPLE_PAIRED_SCANNERS,
    DISCONNECTED,
    LOW_BATTERY,
    UNEXPECTED_ERROR
}
