package com.simprints.infra.enrolment.records.repository.domain.models

import android.os.Parcelable
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
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
    var fingerprintSamples: List<FingerprintSample> = emptyList(),
    var faceSamples: List<FaceSample> = emptyList(),
) : Parcelable
