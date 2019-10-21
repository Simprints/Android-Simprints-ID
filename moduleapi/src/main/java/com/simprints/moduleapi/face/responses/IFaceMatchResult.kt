package com.simprints.moduleapi.face.responses

import com.simprints.moduleapi.face.responses.entities.IFaceTier

interface IFaceMatchResult {
    val guid: String
    val confidence: Int
    val tier: IFaceTier
}
