package com.simprints.id.data.db.subject

import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface SubjectRepositoryUpSyncHelper {
    suspend fun executeUploadWithProgress(scope: CoroutineScope): ReceiveChannel<SubjectsUpSyncProgress>
}
