package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.EventPayloadType

@Keep
enum class ApiEventPayloadType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove
}

fun EventPayloadType.fromDomainToApi() = when(this) {
    EventPayloadType.EnrolmentRecordCreation -> ApiEventPayloadType.EnrolmentRecordCreation
    EventPayloadType.EnrolmentRecordDeletion -> ApiEventPayloadType.EnrolmentRecordDeletion
    EventPayloadType.EnrolmentRecordMove -> ApiEventPayloadType.EnrolmentRecordMove
}
