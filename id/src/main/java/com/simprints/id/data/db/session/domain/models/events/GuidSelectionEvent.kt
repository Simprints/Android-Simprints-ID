package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class GuidSelectionEvent(starTime: Long,
                         val selectedId: String) : Event(EventType.GUID_SELECTION, starTime)
