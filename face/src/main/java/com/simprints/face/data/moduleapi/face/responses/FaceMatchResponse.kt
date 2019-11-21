package com.simprints.face.data.moduleapi.face.responses

import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceMatchResponse(val result: List<FaceMatchResult>,
                             override val type: FaceResponseType = FaceResponseType.MATCH): FaceResponse
