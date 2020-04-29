package com.simprints.id.orchestrator.steps.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.FaceExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class CoreFaceExitFormResponse(val reason: FaceExitFormReason = FaceExitFormReason.OTHER,
                               val optionalText: String = "") : Parcelable, CoreResponse(CoreResponseType.FACE_EXIT_FORM)
