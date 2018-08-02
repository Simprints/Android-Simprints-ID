package com.simprints.id.data.analytics.events

import com.simprints.id.tools.json.JsonHelper
import io.realm.RealmObject

open class RlEvent : RealmObject {
    private var typeEventDescription: String? = null
    var jsonEvent: String? = null

    fun saveType(type: EventType) {
        this.typeEventDescription = type.toString()
    }

    constructor() {}

    constructor(event: Event): this() {
        saveType(event.type)
        when (event) {
            is CalloutEvent -> jsonEvent = JsonHelper.toJson(event)
            is LoginEvent -> jsonEvent = JsonHelper.toJson(event)
        }
    }

    fun getEvent(): Event? {
        return when (getEvent()) {
            EventType.CALLOUT -> {
                JsonHelper.gson.fromJson(jsonEvent, CalloutEvent::class.java)
            }
            EventType.LOGIN -> {
                JsonHelper.gson.fromJson(jsonEvent, LoginEvent::class.java)
            }
            else -> { null }
        }
    }
}
