package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.ProjectState

@Keep
enum class ApiProjectState {
    RUNNING,
    PAUSED,
    ENDING,
    ENDED,
    ;

    fun toDomain(): ProjectState = when (this) {
        RUNNING -> ProjectState.RUNNING
        PAUSED -> ProjectState.PROJECT_PAUSED
        ENDING -> ProjectState.PROJECT_ENDING
        ENDED -> ProjectState.PROJECT_ENDED
    }
}
