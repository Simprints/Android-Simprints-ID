package com.simprints.id.data.db.syncinfo.local

import com.simprints.id.data.db.syncinfo.local.models.DbSyncInfo
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope

interface SyncInfoLocalDataSource {

    fun load(subSyncScope: SubSyncScope): DbSyncInfo
    fun delete(subSyncScope: SubSyncScope)
}
