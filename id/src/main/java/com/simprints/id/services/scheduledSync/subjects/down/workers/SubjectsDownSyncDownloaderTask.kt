package com.simprints.id.services.scheduledSync.subjects.down.workers

import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.services.scheduledSync.subjects.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.subjects.master.internal.SubjectsSyncCache
import kotlinx.coroutines.CoroutineScope

interface SubjectsDownSyncDownloaderTask {
    suspend fun execute(workerId: String,
                        downSyncOperation: SubjectsDownSyncOperation,
                        subjectsSyncCache: SubjectsSyncCache,
                        personRepository: SubjectRepository,
                        reporter: WorkerProgressCountReporter,
                        downloadScope: CoroutineScope): Int
}
