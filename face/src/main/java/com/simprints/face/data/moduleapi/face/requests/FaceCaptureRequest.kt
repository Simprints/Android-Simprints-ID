package com.simprints.face.data.moduleapi.face.requests

import com.simprints.face.data.db.person.FaceSample
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceCaptureRequest(val nFaceSamplesToCapture: Int) : FaceRequest

fun IFaceSample.fromModuleApiToDomainFaceSample() = FaceSample(template)
