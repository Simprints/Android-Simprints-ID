package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
sealed class EnrolmentRecordEvent {
    abstract val id: String
    abstract val type: EnrolmentRecordEventType
}
