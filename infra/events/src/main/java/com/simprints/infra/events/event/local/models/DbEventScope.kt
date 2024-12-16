package com.simprints.infra.events.event.local.models

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopePayload
import com.simprints.infra.events.event.domain.models.scope.EventScopeType

@Keep
@Entity
internal data class DbEventScope(
    @PrimaryKey val id: String,
    val projectId: String,
    val type: EventScopeType,
    @Embedded("start_") val createdAt: DbTimestamp,
    @Embedded("end_") val endedAt: DbTimestamp?,
    // Payload is a collection of data that is not directly used in the app,
    // but it is reported to the backend in session scope header.
    val payloadJson: String,
)

internal fun EventScope.fromDomainToDb(jsonHelper: JsonHelper): DbEventScope = DbEventScope(
    id = id,
    projectId = projectId,
    type = type,
    createdAt = createdAt.fromDomainToDb(),
    endedAt = endedAt?.fromDomainToDb(),
    payloadJson = jsonHelper.toJson(payload),
)

internal fun DbEventScope.fromDbToDomain(jsonHelper: JsonHelper): EventScope = EventScope(
    id = id,
    projectId = projectId,
    type = type,
    createdAt = createdAt.fromDbToDomain(),
    endedAt = endedAt?.fromDbToDomain(),
    payload = jsonHelper.fromJson(payloadJson, object : TypeReference<EventScopePayload>() {}),
)
