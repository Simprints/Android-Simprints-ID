package com.simprints.face.controllers.core.events

import com.simprints.face.controllers.core.events.model.Event

interface FaceSessionEventsManager {

    fun addEventInBackground(event: Event)
    fun addEvent(event: Event)
}
