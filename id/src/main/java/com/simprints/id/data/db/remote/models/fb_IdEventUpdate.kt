package com.simprints.id.data.db.remote.models

import com.google.firebase.database.ServerValue
import com.simprints.id.data.db.remote.tools.Utils

class fb_IdEventUpdate(
    var selectedId: String,
    var projectKey: String,
    var androidId: String,
    var date: Long? = null,
    var sessionId: String,
    var serverDate: Map<String, String>) {

    constructor(projectKey: String?,
                selectedId: String,
                androidId: String,
                sessionId: String): this (
        projectKey = projectKey ?: "",
        selectedId = selectedId,
        androidId = androidId,
        date = Utils.now().time,
        sessionId = sessionId,
        serverDate = ServerValue.TIMESTAMP)
}
