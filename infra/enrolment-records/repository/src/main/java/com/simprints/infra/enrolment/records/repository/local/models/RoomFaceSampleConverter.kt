package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.enrolment.records.room.store.models.DbFaceSample as RoomFaceSample

internal fun RoomFaceSample.toDomain(): FaceSample = FaceSample(
    id = uuid,
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun FaceSample.toRoomDb(subjectId: String): RoomFaceSample = RoomFaceSample(
    uuid = id,
    template = template,
    format = format,
    subjectId = subjectId,
    referenceId = referenceId,
)
