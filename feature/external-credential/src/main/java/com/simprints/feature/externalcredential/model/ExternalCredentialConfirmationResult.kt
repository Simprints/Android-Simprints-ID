package com.simprints.feature.externalcredential.model

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.feature.externalcredential.screens.confirmation.ExternalCredentialConfirmationFragmentArgs
import com.simprints.infra.external.credential.store.model.ExternalCredential
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ExternalCredentialConfirmationResult(
    val credential: ExternalCredential.QrCode,
    val result: ExternalCredentialResult
) : Parcelable {
    companion object {
        fun getArgs(
            credential: ExternalCredential.QrCode,
            result: ExternalCredentialResult
        ): Bundle =
            ExternalCredentialConfirmationFragmentArgs(ExternalCredentialConfirmationResult(credential, result)).toBundle()
    }
}

@Keep
enum class ExternalCredentialResult {
    ENROL_OK, ENROL_DUPLICATE_FOUND, SEARCH_FOUND, SEARCH_NOT_FOUND
}
