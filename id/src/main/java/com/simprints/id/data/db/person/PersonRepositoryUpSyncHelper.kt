package com.simprints.id.data.db.person

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface PersonRepositoryUpSyncHelper {
    suspend fun executeUpload(scope: CoroutineScope): ReceiveChannel<Progress>
}
