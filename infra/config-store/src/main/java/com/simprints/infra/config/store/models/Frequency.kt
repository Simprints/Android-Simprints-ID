package com.simprints.infra.config.store.models

enum class Frequency {
    ONLY_PERIODICALLY_UP_SYNC,
    PERIODICALLY,
    PERIODICALLY_AND_ON_SESSION_START,
}
