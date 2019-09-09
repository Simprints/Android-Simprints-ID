package com.simprints.moduleapi.face.responses

import com.simprints.moduleapi.face.responses.entities.IFaceMatchingResult


interface IFaceIdentifyResponse : IFaceResponse {

    val identifications: List<IFaceMatchingResult>
}
