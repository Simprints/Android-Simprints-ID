package com.simprints.id.orchestrator

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.Person

class EnrolmentHelper(private val repository: PersonRepository) {

    suspend fun saveAndUpload(person: Person) {
        repository.saveAndUpload(person)
    }

}
