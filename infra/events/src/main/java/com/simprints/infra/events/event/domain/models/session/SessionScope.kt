package com.simprints.infra.events.event.domain.models.session

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.GeneralConfiguration

@Keep
data class SessionScope(
    val id: String,
    val projectId: String,

    val createdAt: Timestamp,
    var endedAt: Timestamp?,

    val payload: SessionScopePayload,
)

@Keep
data class SessionScopePayload(
    val endCause: SessionEndCause? = null,

    val sidVersion: String,
    val libSimprintsVersion: String,
    val language: String,
    val projectConfigurationUpdatedAt: String,

    val modalities: List<GeneralConfiguration.Modality>,
    val device: Device,
    val databaseInfo: DatabaseInfo,
    val location: Location? = null,
)
