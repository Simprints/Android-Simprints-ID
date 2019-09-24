package com.simprints.face.data.moduleapi.face.responses

import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.moduleapi.face.responses.IFaceResponseType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceCaptureResponse(val capturingResult: List<FaceCaptureResult>,
                               val type: IFaceResponseType = IFaceResponseType.CAPTURE) : FaceResponse
