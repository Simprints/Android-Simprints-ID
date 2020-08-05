package com.simprints.id.data.db.subject

import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource

interface SubjectRepository : SubjectLocalDataSource, FingerprintIdentityLocalDataSource {

    suspend fun saveAndUpload(subject: Subject)

    suspend fun save(subject: Subject)

}
