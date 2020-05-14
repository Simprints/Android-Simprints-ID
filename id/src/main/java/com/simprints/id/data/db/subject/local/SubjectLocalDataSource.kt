package com.simprints.id.data.db.subject.local

import com.simprints.id.data.db.subject.domain.Subject
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface SubjectLocalDataSource : FingerprintIdentityLocalDataSource {

    class Query(val projectId: String? = null,
                val subjectId: String? = null,
                val userId: String? = null,
                val moduleId: String? = null,
                val toSync: Boolean? = null) : Serializable

    suspend fun insertOrUpdate(subjects: List<Subject>)
    suspend fun load(query: Query? = null): Flow<Subject>
    suspend fun delete(queries: List<Query>)
    suspend fun count(query: Query = Query()): Int
}
