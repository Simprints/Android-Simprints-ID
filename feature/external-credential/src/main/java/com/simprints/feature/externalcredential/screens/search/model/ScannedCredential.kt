package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.model.BoundingBox
import java.io.Serializable
import java.util.UUID
import kotlin.toString

@Keep
data class ScannedCredential(
    val credential: TokenizableString.Tokenized,
    val credentialType: ExternalCredentialType,
    val documentImagePath: String?,
    val zoomedCredentialImagePath: String?,
    val credentialBoundingBox: BoundingBox?,
) : Serializable

fun ScannedCredential.toExternalCredential(subjectId: String) = ExternalCredential(
    id = UUID.randomUUID().toString(),
    value = credential,
    subjectId = subjectId,
    type = credentialType,
)
