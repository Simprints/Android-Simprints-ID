package com.simprints.feature.selectsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("SelectSubjectResult")
data class SelectSubjectResult(
    val isSubjectIdSaved: Boolean,
    val credentialSearchResult: ExternalCredentialSearchResult.Complete? = null,
) : StepResult
