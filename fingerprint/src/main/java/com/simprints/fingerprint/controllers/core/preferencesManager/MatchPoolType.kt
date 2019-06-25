package com.simprints.fingerprint.controllers.core.preferencesManager;

import androidx.annotation.Keep
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest.QueryForIdentifyPool
import com.simprints.id.domain.GROUP

@Keep
enum class MatchPoolType {
    USER,
    MODULE,
    PROJECT;

    companion object {
        fun fromQueryForIdentifyPool(queryForIdentifyPool: QueryForIdentifyPool): MatchPoolType =
            when {
                queryForIdentifyPool.userId != null -> USER
                queryForIdentifyPool.moduleId != null -> MODULE
                else -> PROJECT
            }
    }
}
