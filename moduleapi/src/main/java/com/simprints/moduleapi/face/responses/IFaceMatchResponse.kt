package com.simprints.moduleapi.face.responses

interface IFaceMatchResponse: IFaceResponse {
    val result: List<IFaceMatchResult>
}
