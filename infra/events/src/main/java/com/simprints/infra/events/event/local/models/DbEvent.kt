package com.simprints.infra.events.event.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType

@Entity
internal data class DbEvent(
    @PrimaryKey var id: String,
    @Embedded("createdAt_") val createdAt: DbTimestamp,
    val type: EventType,
    val projectId: String? = null,
    val scopeId: String? = null,
    var eventJson: String,
)

internal fun Event.fromDomainToDb(): DbEvent = DbEvent(
    id = id,
    scopeId = scopeId,
    projectId = projectId,
    type = payload.type,
    eventJson = toJson(),
    createdAt = payload.createdAt.fromDomainToDb(),
)

internal fun DbEvent.fromDbToDomain(): Event = JsonHelper.json.decodeFromString(
    this.eventJson,
)
