package com.simprints.feature.externalcredential.screens.scanqr.usecase

import javax.inject.Inject

internal class ExternalCredentialQrCodeValidatorUseCase @Inject constructor() {
    private val qrCodeLength = 6

    /**
     * Checks whether the scanned QR code value is valid to be used in the Multi-Factor ID. Currently, it uses hardcoded values.
     * In future, the validity criteria should be passed from the project configuration.
     */
    operator fun invoke(qrCodeValue: String): Boolean {
        return qrCodeValue.length == qrCodeLength
    }
}
