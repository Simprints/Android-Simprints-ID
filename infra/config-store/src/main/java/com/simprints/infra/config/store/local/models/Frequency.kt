package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.Frequency

internal fun Frequency.toProto(): ProtoSyncFrequency = when (this) {
    Frequency.ONLY_PERIODICALLY_UP_SYNC -> ProtoSyncFrequency.ONLY_PERIODICALLY_UP_SYNC
    Frequency.PERIODICALLY -> ProtoSyncFrequency.PERIODICALLY
    Frequency.PERIODICALLY_AND_ON_SESSION_START -> ProtoSyncFrequency.PERIODICALLY_AND_ON_SESSION_START
}

internal fun ProtoSyncFrequency.toDomain(): Frequency = when (this) {
    ProtoSyncFrequency.ONLY_PERIODICALLY_UP_SYNC -> Frequency.ONLY_PERIODICALLY_UP_SYNC
    ProtoSyncFrequency.PERIODICALLY -> Frequency.PERIODICALLY
    ProtoSyncFrequency.PERIODICALLY_AND_ON_SESSION_START -> Frequency.PERIODICALLY_AND_ON_SESSION_START
    ProtoSyncFrequency.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid Frequency $name")
}
