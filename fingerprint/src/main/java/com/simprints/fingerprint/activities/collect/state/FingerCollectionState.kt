package com.simprints.fingerprint.activities.collect.state

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

sealed class FingerCollectionState(open val id: FingerIdentifier) {

    data class NotCollected(override val id: FingerIdentifier) : FingerCollectionState(id)
    data class Skipped(override val id: FingerIdentifier) : FingerCollectionState(id)
    data class Scanning(override val id: FingerIdentifier, val numberOfBadScans: Int = 0) : FingerCollectionState(id)
    data class TransferringImage(override val id: FingerIdentifier, val scanResult: ScanResult, val numberOfBadScans: Int = 0) : FingerCollectionState(id)
    data class NotDetected(override val id: FingerIdentifier, val numberOfBadScans: Int = 0) : FingerCollectionState(id)
    data class Collected(override val id: FingerIdentifier, val scanResult: ScanResult, val numberOfBadScans: Int = 0) : FingerCollectionState(id)

    fun isCommunicating(): Boolean = this is Scanning || this is TransferringImage

    fun toNotCollected() = NotCollected(id)

    fun toSkipped() = Skipped(id)

    fun toScanning(): Scanning = when (this) {
        is Scanning -> Scanning(id, numberOfBadScans)
        is TransferringImage -> Scanning(id, numberOfBadScans)
        is NotDetected -> Scanning(id, numberOfBadScans)
        is Collected -> Scanning(id, numberOfBadScans)
        else -> Scanning(id)
    }

    fun toTransferringImage(scanResult: ScanResult): TransferringImage = when (this) {
        is TransferringImage -> TransferringImage(id, scanResult, numberOfBadScans)
        is Scanning -> TransferringImage(id, scanResult, numberOfBadScans)
        is NotDetected -> TransferringImage(id, scanResult, numberOfBadScans)
        is Collected -> TransferringImage(id, scanResult, numberOfBadScans)
        else -> TransferringImage(id, scanResult)
    }

    fun toNotDetected(): NotDetected = when (this) {
        is NotDetected -> NotDetected(id, numberOfBadScans)
        is Scanning -> NotDetected(id, numberOfBadScans)
        is TransferringImage -> NotDetected(id, numberOfBadScans)
        is Collected -> NotDetected(id, numberOfBadScans)
        else -> NotDetected(id)
    }

    fun toCollected(scanResult: ScanResult): Collected = when (this) {
        is Scanning -> Collected(id, scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is TransferringImage -> Collected(id, scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is NotDetected -> Collected(id, scanResult, numberOfBadScans + incIfBadScan(scanResult))
        is Collected -> Collected(id, scanResult, numberOfBadScans + incIfBadScan(scanResult))
        else -> Collected(id, scanResult, incIfBadScan(scanResult))
    }

    private fun incIfBadScan(scanResult: ScanResult) =
        if (scanResult.isGoodScan()) 0 else 1

    fun toCollected(imageBytes: ByteArray): Collected = when (this) {
        is TransferringImage -> toCollected(scanResult.copy(image = imageBytes))
        is Collected -> Collected(id, scanResult.copy(image = imageBytes), numberOfBadScans)
        else -> throw IllegalStateException("Illegal attempt to move to collected state without scan result")
    }
}
