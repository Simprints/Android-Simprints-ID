package com.simprints.id.data.db.remote.models

import com.google.firebase.database.ServerValue
import com.simprints.id.data.db.remote.tools.Utils
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.fingerprint.Person
import java.util.*

class fb_IdEvent (
    var ProbePerson: fb_Person,
    var userId: String,
    var matchSize: Int = 0,
    var sessionId: String,
    var date: Long,
    var fbMatches: MutableList<fb_Match>,
    var serverDate: Map<String, String>) {

    constructor(probe: Person,
                projectId: String,
                userId: String,
                moduleId: String,
                matchSize: Int,
                identifications: List<IdentificationResult>,
                sessionId: String) : this (

        ProbePerson = fb_Person(probe, projectId, userId, moduleId),
        userId = userId,
        matchSize = matchSize,
        sessionId = sessionId,
        date = Utils.now().time,
        fbMatches = ArrayList(),
        serverDate = ServerValue.TIMESTAMP) {

        identifications.forEach { fbMatches.add(fb_Match(it)) }
    }
}

