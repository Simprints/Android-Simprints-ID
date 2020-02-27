package com.simprints.id.data.db.session.local.models

import com.simprints.id.data.db.session.domain.models.session.DatabaseInfo
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DbDatabaseInfo : RealmObject {

    @PrimaryKey
    lateinit var id: String

    var sessionCount: Int = 0
    var recordCount: Int? = null

    constructor()

    constructor(databaseInfo: DatabaseInfo) : this() {
        id = databaseInfo.id
        recordCount = databaseInfo.recordCount
        sessionCount = databaseInfo.sessionCount
    }
}

fun DbDatabaseInfo.toDomainDatabaseInfo(): DatabaseInfo = DatabaseInfo(sessionCount, recordCount, id)
