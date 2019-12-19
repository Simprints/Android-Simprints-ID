package com.simprints.id.data.db.person.local

import com.simprints.id.data.db.person.domain.Person
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface PersonLocalDataSource : FingerprintIdentityLocalDataSource {

    class Query(val projectId: String? = null,
                val personId: String? = null,
                val userId: String? = null,
                val moduleId: String? = null,
                val toSync: Boolean? = null) : Serializable

    suspend fun insertOrUpdate(people: List<Person>)
    suspend fun load(query: Query? = null): Flow<Person>
    suspend fun delete(queries: List<Query>)
    fun count(query: Query = Query()): Int
}
