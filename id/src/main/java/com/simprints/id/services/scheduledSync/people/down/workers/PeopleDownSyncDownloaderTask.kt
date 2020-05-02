package com.simprints.id.services.scheduledSync.people.down.workers

import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import kotlinx.coroutines.CoroutineScope

interface PeopleDownSyncDownloaderTask {
    suspend fun execute(workerId: String,
                        downSyncOperation: PeopleDownSyncOperation,
                        peopleSyncCache: PeopleSyncCache,
                        personRepository: PersonRepository,
                        reporter: WorkerProgressCountReporter,
                        downloadScope: CoroutineScope): Int
}
