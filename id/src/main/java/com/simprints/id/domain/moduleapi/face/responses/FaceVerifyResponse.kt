package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchingResult
import com.simprints.id.domain.moduleapi.face.responses.entities.fromDomainApiToDomain
import com.simprints.moduleapi.face.responses.IFaceVerifyResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceVerifyResponse(val matchingResult: FaceMatchingResult) : FaceResponse()

fun IFaceVerifyResponse.fromModuleApiToDomain(): FaceVerifyResponse = FaceVerifyResponse(matchingResult.fromDomainApiToDomain())
