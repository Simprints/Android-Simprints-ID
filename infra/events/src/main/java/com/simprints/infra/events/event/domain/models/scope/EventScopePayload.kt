package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.GeneralConfiguration

@Keep
data class EventScopePayload(
    val endCause: EventScopeEndCause? = null,
    val sidVersion: String,
    val libSimprintsVersion: String,
    val language: String,
    val projectConfigurationUpdatedAt: String,
    val projectConfigurationId: String? = null,
    val modalities: List<GeneralConfiguration.Modality>,
    val device: Device,
    val databaseInfo: DatabaseInfo,
    val location: Location? = null,
)
