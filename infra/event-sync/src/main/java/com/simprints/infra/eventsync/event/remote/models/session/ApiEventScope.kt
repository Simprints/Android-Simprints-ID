package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.eventsync.event.remote.models.ApiEvent
import com.simprints.infra.eventsync.event.remote.models.ApiModality
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp

@Keep
internal data class ApiEventScope(
    val id: String,
    val projectId: String,
    val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val endCause: ApiEventScopeEndCause,
    val modalities: List<ApiModality>,
    val sidVersion: String,
    val libSimprintsVersion: String,
    val language: String,
    val device: ApiDevice,
    val databaseInfo: ApiDatabaseInfo,
    val location: ApiLocation?,
    @JsonInclude(Include.NON_EMPTY)
    val projectConfigurationUpdatedAt: String,
    val projectConfigurationId: String,
    val events: List<ApiEvent>,
)
