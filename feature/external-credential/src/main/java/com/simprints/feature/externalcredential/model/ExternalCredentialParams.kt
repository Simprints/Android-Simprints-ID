package com.simprints.feature.externalcredential.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ExternalCredentialParams(
    val subjectId: String?,
    val flowType: FlowType
) : Parcelable
