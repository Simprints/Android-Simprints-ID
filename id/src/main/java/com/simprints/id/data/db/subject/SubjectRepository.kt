package com.simprints.id.data.db.subject

import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.common.models.SubjectsCount
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncProgress
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncScope
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncProgress
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface SubjectRepository : SubjectLocalDataSource, FingerprintIdentityLocalDataSource {

    suspend fun countToDownSync(subjectsDownSyncScope: SubjectsDownSyncScope): SubjectsCount

    suspend fun saveAndUpload(subject: Subject)
    suspend fun loadFromRemoteIfNeeded(projectId: String, subjectId: String): SubjectFetchResult


    suspend fun performUploadWithProgress(scope: CoroutineScope): ReceiveChannel<SubjectsUpSyncProgress>
    suspend fun performDownloadWithProgress(scope: CoroutineScope,
                                            subjectsDownSyncOperation: SubjectsDownSyncOperation): ReceiveChannel<SubjectsDownSyncProgress>
}
