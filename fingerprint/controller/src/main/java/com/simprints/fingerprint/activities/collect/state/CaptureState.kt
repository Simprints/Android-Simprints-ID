package com.simprints.fingerprint.activities.collect.state

sealed class CaptureState {

    object NotCollected : CaptureState()
    object Skipped : CaptureState()
    data class Scanning(val numberOfBadScans: Int = 0) : CaptureState()
    data class TransferringImage(val scanResult: ScanResult, val numberOfBadScans: Int = 0) : CaptureState()
    data class NotDetected(val numberOfBadScans: Int = 0) : CaptureState()
    data class Collected(val scanResult: ScanResult, val numberOfBadScans: Int = 0) : CaptureState()

    fun isCommunicating(): Boolean = this is Scanning || this is TransferringImage

    fun toNotCollected() = NotCollected

    fun toSkipped() = Skipped

    fun toScanning(): Scanning = when (this) {
        is Scanning -> Scanning(numberOfBadScans)
        is TransferringImage -> Scanning(numberOfBadScans)
        is NotDetected -> Scanning(numberOfBadScans)
        is Collected -> Scanning(numberOfBadScans)
        else -> Scanning()
    }

    fun toTransferringImage(scanResult: ScanResult): TransferringImage = when (this) {
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

    fun toCollected(scanResult: ScanResult): Collected = when (this) {
        is Scanning -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is TransferringImage -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is NotDetected -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is Collected -> Collected(scanResult, numberOfBadScans + incIfBadScan(scanResult))
        else -> Collected(scanResult, incIfBadScan(scanResult))
    }

    private fun incIfBadScan(scanResult: ScanResult) =
        if (scanResult.isGoodScan()) 0 else 1

    fun toCollected(imageBytes: ByteArray): Collected = when (this) {
        is TransferringImage -> toCollected(scanResult.copy(image = imageBytes))
        is Collected -> Collected(scanResult.copy(image = imageBytes), numberOfBadScans)
        else -> throw IllegalStateException("Illegal attempt to move to collected state without scan result")
    }
}
