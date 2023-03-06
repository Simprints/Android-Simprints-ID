package com.simprints.infra.events.event.domain.models.session

import androidx.annotation.Keep

@Keep
data class DatabaseInfo(val sessionCount: Int,
                        var recordCount: Int? = null)
