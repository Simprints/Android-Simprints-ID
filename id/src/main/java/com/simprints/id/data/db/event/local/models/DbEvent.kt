package com.simprints.id.data.db.event.local.models

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.domain.events.EventPayloadType.valueOf
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DbEvent : RealmObject {

    @PrimaryKey
    lateinit var id: String

    var typeEventDescription: String? = null
    var jsonEvent: String? = null

    private fun saveType(type: EventPayloadType) {
        this.typeEventDescription = type.toString()
    }

    fun getType(): EventPayloadType? = typeEventDescription?.let { valueOf(it) }

    constructor()
    constructor(event: Event) : this() {
        saveType(event.payload.type)
        id = event.id
        jsonEvent = JsonHelper.toJson(event)
    }
}
