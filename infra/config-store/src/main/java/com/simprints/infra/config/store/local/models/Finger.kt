package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.Finger

internal fun Finger.toProto(): ProtoFinger = when (this) {
    Finger.LEFT_THUMB -> ProtoFinger.LEFT_THUMB
    Finger.LEFT_INDEX_FINGER -> ProtoFinger.LEFT_INDEX_FINGER
    Finger.LEFT_3RD_FINGER -> ProtoFinger.LEFT_3RD_FINGER
    Finger.LEFT_4TH_FINGER -> ProtoFinger.LEFT_4TH_FINGER
    Finger.LEFT_5TH_FINGER -> ProtoFinger.LEFT_5TH_FINGER
    Finger.RIGHT_THUMB -> ProtoFinger.RIGHT_THUMB
    Finger.RIGHT_INDEX_FINGER -> ProtoFinger.RIGHT_INDEX_FINGER
    Finger.RIGHT_3RD_FINGER -> ProtoFinger.RIGHT_3RD_FINGER
    Finger.RIGHT_4TH_FINGER -> ProtoFinger.RIGHT_4TH_FINGER
    Finger.RIGHT_5TH_FINGER -> ProtoFinger.RIGHT_5TH_FINGER
}

internal fun ProtoFinger.toDomain(): Finger = when (this) {
    ProtoFinger.LEFT_THUMB -> Finger.LEFT_THUMB
    ProtoFinger.LEFT_INDEX_FINGER -> Finger.LEFT_INDEX_FINGER
    ProtoFinger.LEFT_3RD_FINGER -> Finger.LEFT_3RD_FINGER
    ProtoFinger.LEFT_4TH_FINGER -> Finger.LEFT_4TH_FINGER
    ProtoFinger.LEFT_5TH_FINGER -> Finger.LEFT_5TH_FINGER
    ProtoFinger.RIGHT_THUMB -> Finger.RIGHT_THUMB
    ProtoFinger.RIGHT_INDEX_FINGER -> Finger.RIGHT_INDEX_FINGER
    ProtoFinger.RIGHT_3RD_FINGER -> Finger.RIGHT_3RD_FINGER
    ProtoFinger.RIGHT_4TH_FINGER -> Finger.RIGHT_4TH_FINGER
    ProtoFinger.RIGHT_5TH_FINGER -> Finger.RIGHT_5TH_FINGER
    ProtoFinger.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid Finger $name")
}
