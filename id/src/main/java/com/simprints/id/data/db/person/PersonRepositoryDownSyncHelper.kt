package com.simprints.id.data.db.person

import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface PersonRepositoryDownSyncHelper {
    suspend fun performDownSyncWithProgress(scope: CoroutineScope,
                                            downSyncOperation: PeopleDownSyncOperation,
                                            eventQuery: EventQuery): ReceiveChannel<PeopleDownSyncProgress>
}
