package com.simprints.id.data.db.people_sync.down.domain

import com.simprints.id.data.db.person.domain.personevents.EventPayloadType
import com.simprints.id.domain.modality.Modes

data class EventQuery(val projectId: String,
                      val userId: String? = null,
                      val moduleIds: List<String>? = null,
                      val subjectId: String? = null,
                      val lastEventId: String? = null,
                      val modes: List<Modes>,
                      val types: List<EventPayloadType>)
