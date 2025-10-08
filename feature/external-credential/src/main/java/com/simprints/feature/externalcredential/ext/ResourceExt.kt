package com.simprints.feature.externalcredential.ext

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.simprints.core.domain.externalcredential.ExternalCredentialType

fun Resources.getQuantityCredentialString(
    @PluralsRes id: Int,
    @StringRes specificCredentialRes: Int,
    @StringRes multipleCredentialsRes: Int,
    credentialTypes: List<ExternalCredentialType>,
): String {
    val credentialsAmount = credentialTypes.size
    val documentTypeRes = if (credentialsAmount == 1) {
        specificCredentialRes
    } else {
        multipleCredentialsRes
    }
    return getQuantityString(
        id,
        credentialsAmount,
        getString(documentTypeRes),
    )
}
