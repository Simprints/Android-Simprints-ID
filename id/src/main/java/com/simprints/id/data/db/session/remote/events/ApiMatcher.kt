package com.simprints.id.data.db.session.remote.events

import com.simprints.id.data.db.session.domain.models.events.Matcher

enum class ApiMatcher {

    SIM_AFIS,
    RANK_ONE

}

fun Matcher.fromDomainToApi() = when (this) {
    Matcher.SIM_AFIS -> ApiMatcher.SIM_AFIS
    Matcher.RANK_ONE -> ApiMatcher.RANK_ONE
}
