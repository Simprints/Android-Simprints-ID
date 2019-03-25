package com.simprints.moduleapi.face.responses


interface IFaceIdentifyResponse : IFaceResponse {

    val identifications: List<IFaceMatchingResult>
}
