package com.simprints.face.capture

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.infra.enrolment.records.store.domain.models.TemplateAuxData
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FaceCaptureParams(
    val samplesToCapture: Int,
    val auxData: TemplateAuxData?,
) : Parcelable
