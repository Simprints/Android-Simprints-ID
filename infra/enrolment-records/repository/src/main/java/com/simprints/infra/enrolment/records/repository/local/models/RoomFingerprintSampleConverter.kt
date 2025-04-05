package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.enrolment.records.room.store.models.DbFingerprintSample as RoomFingerprintSample

internal fun RoomFingerprintSample.toDomain(): FingerprintSample = FingerprintSample(
    id = uuid,
    fingerIdentifier = IFingerIdentifier.entries[fingerIdentifier],
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun FingerprintSample.toRoomDb(subjectId: String) = RoomFingerprintSample(
    uuid = id,
    fingerIdentifier = fingerIdentifier.ordinal,
    template = template,
    format = format,
    subjectId = subjectId,
    referenceId = referenceId,
)
