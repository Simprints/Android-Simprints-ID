package com.simprints.id.data.db.remote.models

import com.google.firebase.database.ServerValue
import com.simprints.id.domain.refusal_form.IdRefusalForm

class fb_RefusalForm(
    var reason: String,
    var otherText: String,
    var projectId: String,
    var userId: String,
    var sessionId: String,
    var serverTimestamp: Map<String, String>) {

    constructor(refusalForm: IdRefusalForm,
                projectId: String,
                userId: String,
                sessionId: String): this (
        reason = refusalForm.reason ?: "",
        otherText = refusalForm.extra ?: "",
        projectId = projectId,
        userId = userId,
        sessionId = sessionId,
        serverTimestamp = ServerValue.TIMESTAMP
    )
}
