package com.simprints.id.data.db.person

import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Single

interface PersonRepository {
    fun countToDownSync(syncScope: SyncScope): Single<List<PeopleCount>>
    fun localCountForSyncScope(syncScope: SyncScope): Single<List<PeopleCount>>
}
