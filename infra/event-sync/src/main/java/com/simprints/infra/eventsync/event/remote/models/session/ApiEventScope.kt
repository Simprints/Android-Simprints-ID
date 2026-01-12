package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.ApiEvent
import com.simprints.infra.eventsync.event.remote.models.ApiModality
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Keep
@Serializable
internal data class ApiEventScope(
    val id: String,
    val projectId: String,
    val startTime: ApiTimestamp,
    val endTime: ApiTimestamp? = null,
    val endCause: ApiEventScopeEndCause,
    val modalities: List<ApiModality>,
    val sidVersion: String,
    val libSimprintsVersion: String,
    val language: String,
    val device: ApiDevice,
    val databaseInfo: ApiDatabaseInfo,
    val location: ApiLocation? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val projectConfigurationUpdatedAt: String = "",
    val projectConfigurationId: String,
    val events: List<ApiEvent>,
)
