package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.model.BoundingBox
import java.io.Serializable

@Keep
data class ScannedCredential(
    val credential: String,
    val credentialType: ExternalCredentialType,
    val previewImagePath: String?,
    val imageBoundingBox: BoundingBox?
) : Serializable
