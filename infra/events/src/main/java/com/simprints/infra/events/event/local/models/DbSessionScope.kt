package com.simprints.infra.events.event.local.models

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.domain.models.session.SessionScopePayload

@Keep
@Entity
internal data class DbSessionScope(
    @PrimaryKey val id: String,
    val projectId: String,
    @Embedded("start_") val createdAt: DbTimestamp,
    @Embedded("end_") val endedAt: DbTimestamp?,

    // Payload is a collection of data that is not directly used in the app,
    // but it is reported to the backend in session scope header.
    val payloadJson: String,
)

internal fun SessionScope.fromDomainToDb(jsonHelper: JsonHelper): DbSessionScope = DbSessionScope(
    id = id,
    projectId = projectId,
    createdAt = createdAt.fromDomainToDb(),
    endedAt = endedAt?.fromDomainToDb(),
    payloadJson = jsonHelper.toJson(payload)
)

internal fun DbSessionScope.fromDbToDomain(jsonHelper: JsonHelper): SessionScope = SessionScope(
    id = id,
    projectId = projectId,
    createdAt = createdAt.fromDbToDomain(),
    endedAt = endedAt?.fromDbToDomain(),
    payload = jsonHelper.fromJson(payloadJson, object : TypeReference<SessionScopePayload>() {})
)
