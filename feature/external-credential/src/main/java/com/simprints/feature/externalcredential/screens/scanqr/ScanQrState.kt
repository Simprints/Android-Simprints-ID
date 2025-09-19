package com.simprints.feature.externalcredential.screens.scanqr

sealed class ScanQrState {
    data object NothingScanned : ScanQrState()

    data class ScanComplete(
        val qrCodeValue: String
    ) : ScanQrState()
}
