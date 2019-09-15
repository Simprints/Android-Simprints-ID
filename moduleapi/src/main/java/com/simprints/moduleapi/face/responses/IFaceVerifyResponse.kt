package com.simprints.moduleapi.face.responses

import com.simprints.moduleapi.face.responses.entities.IFaceMatchingResult


interface IFaceVerifyResponse : IFaceResponse {

    val matchingResult: IFaceMatchingResult
}
