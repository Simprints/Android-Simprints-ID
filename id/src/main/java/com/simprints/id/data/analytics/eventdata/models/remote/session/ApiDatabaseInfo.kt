package com.simprints.id.data.analytics.eventdata.models.remote.session

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo

@Keep
open class ApiDatabaseInfo(var recordCount: Int?,
                           var sessionCount: Int = 0) {
    constructor(databaseInfo: DatabaseInfo) :
        this(databaseInfo.recordCount, databaseInfo.sessionCount)
}
