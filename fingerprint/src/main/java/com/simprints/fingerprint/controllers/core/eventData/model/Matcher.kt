package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Matcher as CoreMatcher

@Keep
enum class Matcher {
    SIM_AFIS
}

fun Matcher.fromDomainToCore() =
    when (this) {
        Matcher.SIM_AFIS -> CoreMatcher.SIM_AFIS
    }
