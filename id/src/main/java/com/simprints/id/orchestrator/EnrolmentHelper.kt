package com.simprints.id.orchestrator

import com.simprints.id.data.db.person.domain.Person

interface EnrolmentHelper {

    suspend fun saveAndUpload(person: Person)
    fun registerEvent(person: Person)

}
