package com.simprints.infra.enrolment.records.repository.domain.models

import android.os.Parcelable
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Subject(
    val subjectId: String,
    val projectId: String,
    val attendantId: TokenizableString,
    val moduleId: TokenizableString,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var fingerprintSamples: List<Sample> = emptyList(),
    var faceSamples: List<Sample> = emptyList(),
    var externalCredentials: List<ExternalCredential> = emptyList(),
) : Parcelable
