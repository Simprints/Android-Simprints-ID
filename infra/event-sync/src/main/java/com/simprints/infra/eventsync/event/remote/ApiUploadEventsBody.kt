package com.simprints.infra.eventsync.event.remote

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.session.ApiEventScope

@Keep
internal data class ApiUploadEventsBody(
    val sessions: List<ApiEventScope> = emptyList(),
    val eventUpSyncs: List<ApiEventScope> = emptyList(),
    val eventDownSyncs: List<ApiEventScope> = emptyList(),
    val sampleUpSyncs: List<ApiEventScope> = emptyList(),
)
