package com.simprints.feature.externalcredential.screens.scanqr

import com.simprints.core.domain.tokenization.TokenizableString

sealed class ScanQrState {
    data object ReadyToScan : ScanQrState()

    data class NoCameraPermission(
        val shouldOpenPhoneSettings: Boolean
    ) : ScanQrState()

    data class QrCodeCaptured(
        val qrCode: TokenizableString.Raw,
        val qrCodeEncrypted: TokenizableString.Tokenized
    ) : ScanQrState()
}
