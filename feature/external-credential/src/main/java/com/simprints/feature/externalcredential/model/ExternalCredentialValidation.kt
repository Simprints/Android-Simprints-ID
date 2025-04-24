package com.simprints.feature.externalcredential.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.infra.external.credential.store.model.ExternalCredential
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ExternalCredentialValidation(
    val credential: ExternalCredential,
    val result: ExternalCredentialResult
) : Parcelable

@Keep
enum class ExternalCredentialResult {
    ENROL_OK, ENROL_DUPLICATE_FOUND, SEARCH_FOUND, SEARCH_NOT_FOUND, CREDENTIAL_EMPTY
}
