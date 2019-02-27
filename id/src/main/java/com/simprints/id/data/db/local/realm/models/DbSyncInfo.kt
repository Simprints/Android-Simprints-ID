package com.simprints.id.data.db.local.realm.models

import com.simprints.id.domain.GROUP
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class DbSyncInfo : RealmObject {

    @field:PrimaryKey
    var syncGroupId: Int = 0

    var moduleId: String? = null

    lateinit var lastKnownPatientUpdatedAt: Date
    lateinit var lastKnownPatientId: String
    lateinit var lastSyncTime: Date

    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"
    }

    constructor()

    constructor(syncGroup: GROUP, lastPerson: DbPerson, moduleId: String?) {
        syncGroupId = syncGroup.ordinal
        lastKnownPatientUpdatedAt = lastPerson.updatedAt ?: Date(0)
        lastKnownPatientId = lastPerson.patientId
        lastSyncTime = Date()
        this.moduleId = moduleId
    }
}
