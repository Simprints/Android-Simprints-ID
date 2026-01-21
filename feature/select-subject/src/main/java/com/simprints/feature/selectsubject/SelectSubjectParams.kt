package com.simprints.feature.selectsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("SelectSubjectParams")
data class SelectSubjectParams(
    val projectId: String,
    val subjectId: String,
    val scannedCredential: ScannedCredential?,
) : StepParams
