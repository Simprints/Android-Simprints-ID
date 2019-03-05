package com.simprints.id.data.analytics.eventdata.models.remote.session

import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo
import java.util.*

open class ApiDatabaseInfo(var recordCount: Int = 0,
                           var sessionCount: Int = 0) {
    constructor(databaseInfo: DatabaseInfo) :
        this(databaseInfo.recordCount, databaseInfo.sessionCount)
}
