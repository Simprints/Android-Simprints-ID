package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.*

@Keep
abstract class ApiEventPayload(@Transient val type: ApiEventPayloadType)

fun EventPayload.fromDomainToApi() = when(this.type) {
    EventPayloadType.EnrolmentRecordCreation -> ApiEnrolmentRecordCreationPayload(this as EnrolmentRecordCreationPayload)
    EventPayloadType.EnrolmentRecordDeletion -> ApiEnrolmentRecordDeletionPayload(this as EnrolmentRecordDeletionPayload)
    EventPayloadType.EnrolmentRecordMove -> ApiEnrolmentRecordMovePayload(this as EnrolmentRecordMovePayload)
}
