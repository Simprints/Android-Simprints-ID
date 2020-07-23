package com.simprints.id.data.db.event.remote.events.face

import com.simprints.id.data.db.event.domain.models.Matcher

enum class ApiMatcher {

    SIM_AFIS,
    RANK_ONE

}

fun Matcher.fromDomainToApi() = when (this) {
    Matcher.SIM_AFIS -> ApiMatcher.SIM_AFIS
    Matcher.RANK_ONE -> ApiMatcher.RANK_ONE
}
