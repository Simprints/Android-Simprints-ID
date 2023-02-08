package com.simprints.eventsystem.event.remote.models.subject

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = ApiEnrolmentRecordCreationPayload::class,
        name = ApiEnrolmentRecordPayloadType.ENROLMENT_RECORD_CREATION_KEY
    ),
    JsonSubTypes.Type(
        value = ApiEnrolmentRecordDeletionPayload::class,
        name = ApiEnrolmentRecordPayloadType.ENROLMENT_RECORD_DELETION_KEY
    ),
    JsonSubTypes.Type(
        value = ApiEnrolmentRecordMovePayload::class,
        name = ApiEnrolmentRecordPayloadType.ENROLMENT_RECORD_MOVE_KEY
    ),
)
abstract class ApiEnrolmentRecordEventPayload(
    val type: ApiEnrolmentRecordPayloadType
)
