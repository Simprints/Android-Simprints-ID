package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Matcher as CoreMatcher

// This enum class represents the fingerprint matching algorithm used
@Keep
enum class Matcher {
    SIM_AFIS
}

fun Matcher.fromDomainToCore() =
    when (this) {
        Matcher.SIM_AFIS -> CoreMatcher.SIM_AFIS
    }
