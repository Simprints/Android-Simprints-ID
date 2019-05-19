package com.simprints.fingerprint.controllers.core.preferencesManager;

import androidx.annotation.Keep
import com.simprints.id.domain.GROUP

@Keep
enum class MatchPoolType {
    USER,
    MODULE,
    PROJECT;

    companion object {
        fun fromConstantGroup(constantGroup: GROUP): MatchPoolType {
            return when (constantGroup) {
                GROUP.GLOBAL -> PROJECT
                GROUP.USER -> USER
                GROUP.MODULE -> MODULE
            }
        }
    }
}
