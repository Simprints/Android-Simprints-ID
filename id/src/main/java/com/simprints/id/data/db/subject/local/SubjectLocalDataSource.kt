package com.simprints.id.data.db.subject.local

import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.SubjectAction
import kotlinx.coroutines.flow.Flow

interface SubjectLocalDataSource : FaceIdentityLocalDataSource, FingerprintIdentityLocalDataSource {

    suspend fun insertOrUpdate(subjects: List<Subject>)
    suspend fun load(query: SubjectQuery? = null): Flow<Subject>
    suspend fun delete(queries: List<SubjectQuery>)
    suspend fun deleteAll()
    suspend fun count(query: SubjectQuery = SubjectQuery()): Int

    suspend fun performActions(actions: List<SubjectAction>)
}
