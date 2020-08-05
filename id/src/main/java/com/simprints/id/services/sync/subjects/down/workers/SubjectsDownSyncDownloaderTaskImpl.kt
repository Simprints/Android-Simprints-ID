package com.simprints.id.services.sync.subjects.down.workers

import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperation
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.services.sync.subjects.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

class SubjectsDownSyncDownloaderTaskImpl : SubjectsDownSyncDownloaderTask {

    @ExperimentalCoroutinesApi
    override suspend fun execute(workerId: String,
                                 downSyncOperation: EventsDownSyncOperation,
                                 subjectsSyncCache: SubjectsSyncCache,
                                 personRepository: SubjectRepository,
                                 reporter: WorkerProgressCountReporter,
                                 downloadScope: CoroutineScope): Int {

        var count = subjectsSyncCache.readProgress(workerId)
        val totalDownloaded = personRepository.performDownloadWithProgress(downloadScope, downSyncOperation)

        while (!totalDownloaded.isClosedForReceive) {
            totalDownloaded.poll()?.let {
                count += it.progress
                subjectsSyncCache.saveProgress(workerId, count)
                Timber.d("Downsync downloader count : $count for batch : $it")
                reporter.reportCount(count)
            }
        }
        return count
    }
}
