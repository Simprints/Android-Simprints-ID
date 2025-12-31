package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep
import com.simprints.core.domain.common.Modality
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class EventScopePayload(
    val endCause: EventScopeEndCause? = null,
    val sidVersion: String,
    val libSimprintsVersion: String,
    val language: String,
    val projectConfigurationUpdatedAt: String,
    val projectConfigurationId: String? = null,
    val modalities: List<Modality>,
    val device: Device,
    val databaseInfo: DatabaseInfo,
    val location: Location? = null,
)
