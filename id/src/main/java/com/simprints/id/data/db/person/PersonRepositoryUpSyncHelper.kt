package com.simprints.id.data.db.person

import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface PersonRepositoryUpSyncHelper {
    suspend fun executeUploadWithProgress(scope: CoroutineScope): ReceiveChannel<PeopleUpSyncProgress>
}
