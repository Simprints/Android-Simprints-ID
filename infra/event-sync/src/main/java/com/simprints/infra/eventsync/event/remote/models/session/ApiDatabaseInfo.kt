package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiDatabaseInfo(
    var recordCount: Int? = null,
    var sessionCount: Int = 0,
)

internal fun DatabaseInfo.fromDomainToApi() = ApiDatabaseInfo(recordCount, sessionCount)
