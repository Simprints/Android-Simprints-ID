package com.simprints.id.orchestrator.steps.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.FaceExitFormReason
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class EnrolLastBiometricsResponse(val newSubjectId: String) : Parcelable, CoreResponse(CoreResponseType.ENROL_LAST_BIOMETRICS)
