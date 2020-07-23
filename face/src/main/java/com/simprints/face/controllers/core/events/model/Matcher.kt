package com.simprints.face.controllers.core.events.model

import com.simprints.face.exceptions.FaceUnexpectedException
import com.simprints.id.data.db.event.domain.models.Matcher as CoreMatcher

enum class Matcher {
    RANK_ONE, UNKNOWN;

    fun fromDomainToCore(): CoreMatcher =
        when (this) {
            RANK_ONE -> CoreMatcher.RANK_ONE
            UNKNOWN -> throw FaceUnexpectedException("Unknown matcher")
        }
}
