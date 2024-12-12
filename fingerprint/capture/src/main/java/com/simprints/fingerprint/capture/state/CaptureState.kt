package com.simprints.fingerprint.capture.state

internal sealed class CaptureState {
    data object NotCollected : CaptureState()

    data object Skipped : CaptureState()

    sealed class ScanProcess : CaptureState() {
        abstract val numberOfBadScans: Int
        abstract val numberOfNoFingerDetectedScans: Int

        data class Scanning(
            override val numberOfBadScans: Int,
            override val numberOfNoFingerDetectedScans: Int,
        ) : ScanProcess()

        data class TransferringImage(
            override val numberOfBadScans: Int,
            override val numberOfNoFingerDetectedScans: Int,
            val scanResult: ScanResult,
        ) : ScanProcess()

        data class NotDetected(
            override val numberOfBadScans: Int,
            override val numberOfNoFingerDetectedScans: Int,
        ) : ScanProcess()

        data class Collected(
            override val numberOfBadScans: Int,
            override val numberOfNoFingerDetectedScans: Int,
            val scanResult: ScanResult,
        ) : ScanProcess()
    }

    fun isCommunicating(): Boolean = this is ScanProcess.Scanning || this is ScanProcess.TransferringImage

    fun toNotCollected() = NotCollected

    fun toSkipped() = Skipped

    fun toScanning(): ScanProcess.Scanning = when (this) {
        is ScanProcess -> ScanProcess.Scanning(
            numberOfBadScans = numberOfBadScans,
            numberOfNoFingerDetectedScans = numberOfNoFingerDetectedScans,
        )

        else -> ScanProcess.Scanning(
            numberOfBadScans = 0,
            numberOfNoFingerDetectedScans = 0,
        )
    }

    fun toTransferringImage(scanResult: ScanResult): ScanProcess.TransferringImage = when (this) {
        is ScanProcess -> ScanProcess.TransferringImage(
            numberOfBadScans = numberOfBadScans,
            numberOfNoFingerDetectedScans = numberOfNoFingerDetectedScans,
            scanResult = scanResult,
        )

        else -> ScanProcess.TransferringImage(
            numberOfBadScans = 0,
            numberOfNoFingerDetectedScans = 0,
            scanResult = scanResult,
        )
    }

    fun toNotDetected(): ScanProcess.NotDetected = when (this) {
        is ScanProcess -> ScanProcess.NotDetected(
            numberOfBadScans = numberOfBadScans,
            numberOfNoFingerDetectedScans = numberOfNoFingerDetectedScans + 1,
        )

        else -> ScanProcess.NotDetected(
            numberOfBadScans = 0,
            numberOfNoFingerDetectedScans = 0,
        )
    }

    fun toCollected(scanResult: ScanResult): ScanProcess.Collected = when (this) {
        is ScanProcess -> ScanProcess.Collected(
            numberOfBadScans = numberOfBadScans + incIfBadScan(scanResult),
            numberOfNoFingerDetectedScans = numberOfNoFingerDetectedScans,
            scanResult = scanResult,
        )

        else -> ScanProcess.Collected(
            numberOfBadScans = incIfBadScan(scanResult),
            numberOfNoFingerDetectedScans = 0,
            scanResult,
        )
    }

    private fun incIfBadScan(scanResult: ScanResult) = if (scanResult.isGoodScan()) 0 else 1

    fun toCollected(imageBytes: ByteArray): ScanProcess.Collected = when (this) {
        is ScanProcess.TransferringImage -> toCollected(scanResult.copy(image = imageBytes))
        is ScanProcess.Collected -> ScanProcess.Collected(
            numberOfBadScans = numberOfBadScans,
            numberOfNoFingerDetectedScans = numberOfNoFingerDetectedScans,
            scanResult = scanResult.copy(image = imageBytes),
        )

        else -> throw IllegalStateException("Illegal attempt to move to collected state without scan result")
    }
}
