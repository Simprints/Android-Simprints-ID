package com.simprints.id.data.db.local.realm.models

import com.simprints.id.domain.Constants
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class rl_SyncInfo : RealmObject {

    @field:PrimaryKey
    var syncGroupId: Int = 0

    lateinit var lastKnownPatientUpdatedAt: Date
    lateinit var lastKnownPatientId: String
    lateinit var lastSyncTime: Date

    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"
    }

    constructor() {}

    constructor(syncGroup: Constants.GROUP, lastPerson: rl_Person) {
        syncGroupId = syncGroup.ordinal
        lastKnownPatientUpdatedAt = lastPerson.updatedAt ?: Date(0)
        lastKnownPatientId = lastPerson.patientId
        lastSyncTime = Date()
    }
}
