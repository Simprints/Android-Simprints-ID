package com.simprints.id.services.sync.subjects.down.workers

import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperation
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.services.sync.subjects.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCache
import kotlinx.coroutines.CoroutineScope

interface SubjectsDownSyncDownloaderTask {
    suspend fun execute(workerId: String,
                        downSyncOperation: EventsDownSyncOperation,
                        subjectsSyncCache: SubjectsSyncCache,
                        personRepository: SubjectRepository,
                        reporter: WorkerProgressCountReporter,
                        downloadScope: CoroutineScope): Int
}
