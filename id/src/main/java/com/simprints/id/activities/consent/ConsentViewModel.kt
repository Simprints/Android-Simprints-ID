package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.ConsentEvent

class ConsentViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun addConsentEvent(consentEvent: ConsentEvent) {
        inBackground { eventRepository.addOrUpdateEvent(consentEvent) }
    }
}
