package com.simprints.face.data.moduleapi.face.responses.entities

import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceCaptureResult(override val index: Int, override val sample: IFaceSample?) : IFaceCaptureResult
