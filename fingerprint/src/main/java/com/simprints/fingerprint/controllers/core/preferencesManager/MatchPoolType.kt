package com.simprints.fingerprint.controllers.core.preferencesManager

import androidx.annotation.Keep
import java.io.Serializable

@Keep
enum class MatchPoolType {
    USER,
    MODULE,
    PROJECT;

    companion object {

        fun fromMatchingQuery(query: Serializable): MatchPoolType =
            when {
                else -> PROJECT // STOPSHIP : Need to determine MatchPoolType from the matching query
            }
    }
}
