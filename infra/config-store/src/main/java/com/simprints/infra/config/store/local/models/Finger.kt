package com.simprints.infra.config.store.local.models

import com.simprints.core.domain.sample.SampleIdentifier

internal fun SampleIdentifier.toProtoFinger(): ProtoFinger = when (this) {
    SampleIdentifier.LEFT_THUMB -> ProtoFinger.LEFT_THUMB
    SampleIdentifier.LEFT_INDEX_FINGER -> ProtoFinger.LEFT_INDEX_FINGER
    SampleIdentifier.LEFT_3RD_FINGER -> ProtoFinger.LEFT_3RD_FINGER
    SampleIdentifier.LEFT_4TH_FINGER -> ProtoFinger.LEFT_4TH_FINGER
    SampleIdentifier.LEFT_5TH_FINGER -> ProtoFinger.LEFT_5TH_FINGER
    SampleIdentifier.RIGHT_THUMB -> ProtoFinger.RIGHT_THUMB
    SampleIdentifier.RIGHT_INDEX_FINGER -> ProtoFinger.RIGHT_INDEX_FINGER
    SampleIdentifier.RIGHT_3RD_FINGER -> ProtoFinger.RIGHT_3RD_FINGER
    SampleIdentifier.RIGHT_4TH_FINGER -> ProtoFinger.RIGHT_4TH_FINGER
    SampleIdentifier.RIGHT_5TH_FINGER -> ProtoFinger.RIGHT_5TH_FINGER
    else -> ProtoFinger.UNRECOGNIZED
}

internal fun ProtoFinger.toDomain(): SampleIdentifier = when (this) {
    ProtoFinger.UNRECOGNIZED -> SampleIdentifier.NONE
    ProtoFinger.LEFT_THUMB -> SampleIdentifier.LEFT_THUMB
    ProtoFinger.LEFT_INDEX_FINGER -> SampleIdentifier.LEFT_INDEX_FINGER
    ProtoFinger.LEFT_3RD_FINGER -> SampleIdentifier.LEFT_3RD_FINGER
    ProtoFinger.LEFT_4TH_FINGER -> SampleIdentifier.LEFT_4TH_FINGER
    ProtoFinger.LEFT_5TH_FINGER -> SampleIdentifier.LEFT_5TH_FINGER
    ProtoFinger.RIGHT_THUMB -> SampleIdentifier.RIGHT_THUMB
    ProtoFinger.RIGHT_INDEX_FINGER -> SampleIdentifier.RIGHT_INDEX_FINGER
    ProtoFinger.RIGHT_3RD_FINGER -> SampleIdentifier.RIGHT_3RD_FINGER
    ProtoFinger.RIGHT_4TH_FINGER -> SampleIdentifier.RIGHT_4TH_FINGER
    ProtoFinger.RIGHT_5TH_FINGER -> SampleIdentifier.RIGHT_5TH_FINGER
}
