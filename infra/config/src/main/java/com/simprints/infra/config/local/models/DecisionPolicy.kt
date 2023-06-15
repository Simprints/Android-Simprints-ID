package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.DecisionPolicy

internal fun DecisionPolicy.toProto(): ProtoDecisionPolicy =
    ProtoDecisionPolicy.newBuilder()
        .setLow(low)
        .setMedium(medium)
        .setHigh(high)
        .build()


internal fun ProtoDecisionPolicy.toDomain(): DecisionPolicy =
    DecisionPolicy(low, medium, high)
