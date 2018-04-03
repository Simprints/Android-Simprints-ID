package com.simprints.id.data.db.remote.models

import com.simprints.libsimprints.Identification

class fb_Match (
    var personGuid: String,
    var score: Float = 0.toFloat()) {

    constructor(id: Identification): this(
        personGuid = id.guid,
        score = id.confidence)
}
