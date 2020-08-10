package com.simprints.id.data.db.event.remote.models.session

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiDatabaseInfo(var recordCount: Int?,
                           var sessionCount: Int = 0) {
    constructor(databaseInfo: DatabaseInfo) :
        this(databaseInfo.recordCount, databaseInfo.sessionCount)
}

fun DatabaseInfo.fromDomainToApi() =
    ApiDatabaseInfo(recordCount, sessionCount)
