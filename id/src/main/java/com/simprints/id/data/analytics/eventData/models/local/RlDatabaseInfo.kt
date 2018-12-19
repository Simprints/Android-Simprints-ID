package com.simprints.id.data.analytics.eventData.models.local

import com.simprints.id.data.analytics.eventData.models.domain.session.DatabaseInfo
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RlDatabaseInfo : RealmObject {

    @PrimaryKey
    lateinit var id: String

    var recordCount: Int = 0
    var sessionCount: Int = 0

    constructor()

    constructor(databaseInfo: DatabaseInfo) : this() {
        id = databaseInfo.id
        recordCount = databaseInfo.recordCount
        sessionCount = databaseInfo.sessionCount
    }
}

fun RlDatabaseInfo.toDomainDatabaseInfo(): DatabaseInfo = DatabaseInfo(recordCount, sessionCount, id)
