package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiDatabaseInfo(
    var recordCount: Int?,
    var sessionCount: Int = 0,
)

internal fun DatabaseInfo.fromDomainToApi() = ApiDatabaseInfo(recordCount, sessionCount)
