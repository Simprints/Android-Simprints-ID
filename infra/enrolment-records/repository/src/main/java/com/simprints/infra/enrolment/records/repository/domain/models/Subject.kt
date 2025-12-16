package com.simprints.infra.enrolment.records.repository.domain.models

import android.os.Parcelable
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.parcelize.Parcelize
import java.util.Date
import com.simprints.core.domain.reference.BiometricReference as CoreBiometricReference

@Parcelize
data class Subject(
    val subjectId: String,
    val projectId: String,
    val attendantId: TokenizableString,
    val moduleId: TokenizableString,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var references: List<CoreBiometricReference> = emptyList(),
    var externalCredentials: List<ExternalCredential> = emptyList(),
) : Parcelable
