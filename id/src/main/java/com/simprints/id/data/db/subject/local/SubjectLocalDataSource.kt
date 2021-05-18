package com.simprints.eventsystem.subject.local

import com.simprints.eventsystem.subject.domain.Subject
import com.simprints.eventsystem.subject.domain.SubjectAction
import kotlinx.coroutines.flow.Flow

interface SubjectLocalDataSource : FaceIdentityLocalDataSource, FingerprintIdentityLocalDataSource {

    suspend fun insertOrUpdate(subjects: List<Subject>)
    suspend fun load(query: SubjectQuery? = null): Flow<Subject>
    suspend fun delete(queries: List<SubjectQuery>)
    suspend fun deleteAll()
    suspend fun count(query: SubjectQuery = SubjectQuery()): Int

    suspend fun performActions(actions: List<SubjectAction>)
}
