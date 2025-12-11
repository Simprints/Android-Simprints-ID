package com.simprints.infra.config.store.local.models

import com.simprints.core.domain.reference.TemplateIdentifier

internal fun TemplateIdentifier.toProtoFinger(): ProtoFinger = when (this) {
    TemplateIdentifier.LEFT_THUMB -> ProtoFinger.LEFT_THUMB
    TemplateIdentifier.LEFT_INDEX_FINGER -> ProtoFinger.LEFT_INDEX_FINGER
    TemplateIdentifier.LEFT_3RD_FINGER -> ProtoFinger.LEFT_3RD_FINGER
    TemplateIdentifier.LEFT_4TH_FINGER -> ProtoFinger.LEFT_4TH_FINGER
    TemplateIdentifier.LEFT_5TH_FINGER -> ProtoFinger.LEFT_5TH_FINGER
    TemplateIdentifier.RIGHT_THUMB -> ProtoFinger.RIGHT_THUMB
    TemplateIdentifier.RIGHT_INDEX_FINGER -> ProtoFinger.RIGHT_INDEX_FINGER
    TemplateIdentifier.RIGHT_3RD_FINGER -> ProtoFinger.RIGHT_3RD_FINGER
    TemplateIdentifier.RIGHT_4TH_FINGER -> ProtoFinger.RIGHT_4TH_FINGER
    TemplateIdentifier.RIGHT_5TH_FINGER -> ProtoFinger.RIGHT_5TH_FINGER
    else -> ProtoFinger.UNRECOGNIZED
}

internal fun ProtoFinger.toDomain(): TemplateIdentifier = when (this) {
    ProtoFinger.UNRECOGNIZED -> TemplateIdentifier.NONE
    ProtoFinger.LEFT_THUMB -> TemplateIdentifier.LEFT_THUMB
    ProtoFinger.LEFT_INDEX_FINGER -> TemplateIdentifier.LEFT_INDEX_FINGER
    ProtoFinger.LEFT_3RD_FINGER -> TemplateIdentifier.LEFT_3RD_FINGER
    ProtoFinger.LEFT_4TH_FINGER -> TemplateIdentifier.LEFT_4TH_FINGER
    ProtoFinger.LEFT_5TH_FINGER -> TemplateIdentifier.LEFT_5TH_FINGER
    ProtoFinger.RIGHT_THUMB -> TemplateIdentifier.RIGHT_THUMB
    ProtoFinger.RIGHT_INDEX_FINGER -> TemplateIdentifier.RIGHT_INDEX_FINGER
    ProtoFinger.RIGHT_3RD_FINGER -> TemplateIdentifier.RIGHT_3RD_FINGER
    ProtoFinger.RIGHT_4TH_FINGER -> TemplateIdentifier.RIGHT_4TH_FINGER
    ProtoFinger.RIGHT_5TH_FINGER -> TemplateIdentifier.RIGHT_5TH_FINGER
}
