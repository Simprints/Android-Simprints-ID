package com.simprints.id.data.db.subject.remote.models.subjectevents

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.subject.domain.subjectevents.EventPayloadType
import com.simprints.id.data.db.subject.remote.models.subjectevents.SerialisedApiNames.ENROLMENT_RECORD_CREATION_SERIALISED
import com.simprints.id.data.db.subject.remote.models.subjectevents.SerialisedApiNames.ENROLMENT_RECORD_DELETION_SERIALISED
import com.simprints.id.data.db.subject.remote.models.subjectevents.SerialisedApiNames.ENROLMENT_RECORD_MOVE_SERIALISED

@Keep
enum class ApiEventPayloadType(val apiName: String) {
    @SerializedName(ENROLMENT_RECORD_CREATION_SERIALISED) ENROLMENT_RECORD_CREATION(ENROLMENT_RECORD_CREATION_SERIALISED),
    @SerializedName(ENROLMENT_RECORD_DELETION_SERIALISED) ENROLMENT_RECORD_DELETION(ENROLMENT_RECORD_DELETION_SERIALISED),
    @SerializedName(ENROLMENT_RECORD_MOVE_SERIALISED) ENROLMENT_RECORD_MOVE(ENROLMENT_RECORD_MOVE_SERIALISED);
}

private object SerialisedApiNames {
    const val ENROLMENT_RECORD_CREATION_SERIALISED = "EnrolmentRecordCreation"
    const val ENROLMENT_RECORD_DELETION_SERIALISED = "EnrolmentRecordDeletion"
    const val ENROLMENT_RECORD_MOVE_SERIALISED = "EnrolmentRecordMove"
}

fun EventPayloadType.fromDomainToApi() = when(this) {
    EventPayloadType.ENROLMENT_RECORD_CREATION -> ApiEventPayloadType.ENROLMENT_RECORD_CREATION
    EventPayloadType.ENROLMENT_RECORD_DELETION -> ApiEventPayloadType.ENROLMENT_RECORD_DELETION
    EventPayloadType.ENROLMENT_RECORD_MOVE -> ApiEventPayloadType.ENROLMENT_RECORD_MOVE
}
