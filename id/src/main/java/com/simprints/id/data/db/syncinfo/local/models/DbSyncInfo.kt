package com.simprints.id.data.db.syncinfo.local.models

import com.simprints.id.data.db.person.local.models.DbPerson
import com.simprints.id.domain.GROUP
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class DbSyncInfo : RealmObject {

    @field:PrimaryKey
    var syncGroupId: Int = 0

    var moduleId: String? = null

    @Required lateinit var lastKnownPatientUpdatedAt: Date
    @Required lateinit var lastKnownPatientId: String
    @Required lateinit var lastSyncTime: Date

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
