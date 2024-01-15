package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep

@Keep
enum class ApiProjectState {

    RUNNING,
    PROJECT_PAUSED,
    COMPROMISED,
    PROJECT_ENDING,
    PROJECT_ENDED;
}
