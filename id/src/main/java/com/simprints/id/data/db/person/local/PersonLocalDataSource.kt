package com.simprints.id.data.db.person.local

import com.simprints.id.data.db.person.domain.Person
import io.realm.Sort
import kotlinx.coroutines.flow.Flow

interface PersonLocalDataSource {

    class Query(val projectId: String? = null,
                val patientId: String? = null,
                val userId: String? = null,
                val moduleId: String? = null,
                val toSync: Boolean? = null,
                val sortBy: Map<String, Sort>? = null)

    fun insertOrUpdate(people: List<Person>)
    fun load(query: Query): Flow<Person>
    fun delete(query: Query)
    fun count(query: Query = Query()): Int
}
