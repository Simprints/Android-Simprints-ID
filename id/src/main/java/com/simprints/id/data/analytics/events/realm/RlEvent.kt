package com.simprints.id.data.analytics.events.realm

import com.simprints.id.data.analytics.events.models.CalloutEvent
import com.simprints.id.data.analytics.events.models.Event
import com.simprints.id.data.analytics.events.models.EventType
import com.simprints.id.data.analytics.events.models.LoginEvent
import com.simprints.id.tools.json.JsonHelper
import io.realm.RealmObject

open class RlEvent : RealmObject {

    private var typeEventDescription: String? = null

    var jsonEvent: String? = null

    private fun saveType(type: EventType) {
        this.typeEventDescription = type.toString()
    }

    private fun getType(): EventType? = typeEventDescription?.let { EventType.valueOf(it) }

    constructor() {}
    constructor(event: Event) : this() {
        saveType(event.type)
        jsonEvent = JsonHelper.toJson(event)
    }

    fun getEvent(): Event? {
        return when (getType()) {
            EventType.CALLOUT -> {
                JsonHelper.gson.fromJson(jsonEvent, CalloutEvent::class.java)
            }
            EventType.LOGIN -> {
                JsonHelper.gson.fromJson(jsonEvent, LoginEvent::class.java)
            }
            else -> {
                null
            }
        }
    }
}
