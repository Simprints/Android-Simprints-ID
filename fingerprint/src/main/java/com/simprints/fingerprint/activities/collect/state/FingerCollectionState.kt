package com.simprints.fingerprint.activities.collect.state

sealed class FingerCollectionState {

    object NotCollected : FingerCollectionState()
    object Skipped : FingerCollectionState()
    class Scanning(val numberOfBadScans: Int = 0) : FingerCollectionState()
    class TransferringImage(val fingerScanResult: FingerScanResult, val numberOfBadScans: Int = 0) : FingerCollectionState()
    class NotDetected(val numberOfBadScans: Int = 0) : FingerCollectionState()
    class Collected(val fingerScanResult: FingerScanResult, val numberOfBadScans: Int = 0) : FingerCollectionState()

    fun isBusy(): Boolean = this is Scanning || this is TransferringImage

    fun toNotCollected() = NotCollected

    fun toSkipped() = Skipped

    fun toScanning(): Scanning = when (this) {
        is Scanning -> Scanning(numberOfBadScans)
        is TransferringImage -> Scanning(numberOfBadScans)
        is NotDetected -> Scanning(numberOfBadScans)
        is Collected -> Scanning(numberOfBadScans)
        else -> Scanning()
    }

    fun toTransferringImage(scanResult: FingerScanResult): TransferringImage = when (this) {
        is TransferringImage -> TransferringImage(scanResult, numberOfBadScans)
        is Scanning -> TransferringImage(scanResult, numberOfBadScans)
        is NotDetected -> TransferringImage(scanResult, numberOfBadScans)
        is Collected -> TransferringImage(scanResult, numberOfBadScans)
        else -> TransferringImage(scanResult)
    }

    fun toNotDetected(): NotDetected = when (this) {
        is NotDetected -> NotDetected(numberOfBadScans)
        is Scanning -> NotDetected(numberOfBadScans)
        is TransferringImage -> NotDetected(numberOfBadScans)
        is Collected -> NotDetected(numberOfBadScans)
        else -> NotDetected()
    }

    fun toCollected(scanResult: FingerScanResult): Collected = when (this) {
        is Scanning -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is TransferringImage -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is NotDetected -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is Collected -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        else -> Collected(scanResult, incIfBadScan(scanResult))
    }

    private fun incIfBadScan(scanResult: FingerScanResult) =
        if (scanResult.isGoodScan()) 0 else 1

    fun toCollected(imageBytes: ByteArray): Collected = when (this) {
        is TransferringImage -> toCollected(fingerScanResult.copy(image = imageBytes))
        is Collected -> Collected(fingerScanResult.copy(image = imageBytes), numberOfBadScans)
        else -> throw IllegalStateException("Illegal attempt to move to collected state without scan result")
    }
}
