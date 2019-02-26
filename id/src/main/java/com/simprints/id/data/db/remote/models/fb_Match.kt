package com.simprints.id.data.db.remote.models

import com.simprints.id.domain.matching.IdentificationResult

@Deprecated("Remove it with RTDB")
class fb_Match (
    var personGuid: String,
    var score: Float = 0.toFloat()) {

    constructor(id: IdentificationResult): this(
        personGuid = id.guidFound,
        score = id.confidence.toFloat())
}
