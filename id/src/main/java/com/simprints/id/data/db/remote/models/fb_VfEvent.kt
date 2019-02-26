package com.simprints.id.data.db.remote.models

import com.google.firebase.database.ServerValue
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.tools.Utils
import com.simprints.id.domain.identification.VerificationResult
import com.simprints.id.domain.fingerprint.Person

class fb_VfEvent(
    var ProbePerson: fb_Person,
    var userId: String,
    var sessionId: String,
    var date: Long,
    var guid: String,
    var guidExistsResult: String,
    var confidence: Float = 0.toFloat(),
    var serverDate: Map<String, String>) {

    constructor(probe: Person,
                projectId: String,
                userId: String,
                moduleId: String,
                guid: String,
                verification: VerificationResult?,
                sessionId: String,
                guidExistsResult: VERIFY_GUID_EXISTS_RESULT) : this(
        ProbePerson = fb_Person(probe, projectId, userId, moduleId),
        userId = userId,
        guid = guid,
        date = Utils.now().time,
        sessionId = sessionId,
        serverDate = ServerValue.TIMESTAMP,
        guidExistsResult = guidExistsResult.toString(),
        confidence = verification?.let { it.confidence.toFloat() } ?: 0.toFloat())
}
