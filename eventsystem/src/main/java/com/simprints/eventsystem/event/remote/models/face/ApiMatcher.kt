package com.simprints.eventsystem.event.remote.models.face

import com.simprints.eventsystem.event.domain.models.Matcher

enum class ApiMatcher {

    SIM_AFIS,
    RANK_ONE

}

fun Matcher.fromDomainToApi() = when (this) {
    Matcher.SIM_AFIS -> ApiMatcher.SIM_AFIS
    Matcher.RANK_ONE -> ApiMatcher.RANK_ONE
}
