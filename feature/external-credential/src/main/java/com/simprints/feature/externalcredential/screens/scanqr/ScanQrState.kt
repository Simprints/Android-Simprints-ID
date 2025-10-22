package com.simprints.feature.externalcredential.screens.scanqr

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp

sealed class ScanQrState {
    data object ReadyToScan : ScanQrState()

    data class NoCameraPermission(
        val shouldOpenPhoneSettings: Boolean,
    ) : ScanQrState()

    data class QrCodeCaptured(
        val scanStartTime: Timestamp,
        val scanEndTime: Timestamp,
        val qrCode: TokenizableString.Raw,
        val qrCodeEncrypted: TokenizableString.Tokenized,
    ) : ScanQrState()
}
