package com.simprints.id.data.analytics.eventdata.models.local

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.responses.EnrolResponse
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RlEvent : RealmObject {

    @PrimaryKey
    lateinit var id: String

    var typeEventDescription: String? = null
    var jsonEvent: String? = null

    private fun saveType(type: EventType) {
        this.typeEventDescription = type.toString()
    }

    fun getType(): EventType? = typeEventDescription?.let { valueOf(it) }

    constructor()
    constructor(event: Event) : this() {
        saveType(event.type)
        id = event.id
        jsonEvent = JsonHelper.toJson(event)
    }
}
