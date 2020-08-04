package com.simprints.id.data.db.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo

@Keep
data class ApiDatabaseInfo(var recordCount: Int?,
                           var sessionCount: Int = 0) {
    constructor(databaseInfo: DatabaseInfo) :
        this(databaseInfo.recordCount, databaseInfo.sessionCount)
}

fun DatabaseInfo.fromDomainToApi() =
    ApiDatabaseInfo(recordCount, sessionCount)
