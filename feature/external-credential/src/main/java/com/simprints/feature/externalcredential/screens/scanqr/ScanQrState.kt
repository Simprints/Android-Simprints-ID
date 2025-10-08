package com.simprints.feature.externalcredential.screens.scanqr

sealed class ScanQrState {
    data object ReadyToScan : ScanQrState()

    data class NoCameraPermission(
        val shouldOpenPhoneSettings: Boolean,
    ) : ScanQrState()

    data class QrCodeCaptured(
        val qrCodeValue: String,
    ) : ScanQrState()
}
