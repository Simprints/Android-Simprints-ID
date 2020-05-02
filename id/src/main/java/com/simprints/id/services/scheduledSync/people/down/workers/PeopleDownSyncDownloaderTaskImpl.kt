package com.simprints.id.services.scheduledSync.people.down.workers

import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

class PeopleDownSyncDownloaderTaskImpl : PeopleDownSyncDownloaderTask {

    @ExperimentalCoroutinesApi
    override suspend fun execute(workerId: String,
                                 downSyncOperation: PeopleDownSyncOperation,
                                 peopleSyncCache: PeopleSyncCache,
                                 personRepository: PersonRepository,
                                 reporter: WorkerProgressCountReporter,
                                 downloadScope: CoroutineScope): Int {

        var count = peopleSyncCache.readProgress(workerId)
        val totalDownloaded = personRepository.performDownloadWithProgress(downloadScope, downSyncOperation)

        while (!totalDownloaded.isClosedForReceive) {
            totalDownloaded.poll()?.let {
                count += it.progress
                peopleSyncCache.saveProgress(workerId, count)
                Timber.d("Downsync downloader count : $count for batch : $it")
                reporter.reportCount(count)
            }
        }
        return count
    }
}
