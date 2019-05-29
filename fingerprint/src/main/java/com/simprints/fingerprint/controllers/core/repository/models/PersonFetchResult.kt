package com.simprints.fingerprint.controllers.core.repository.models

import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.id.data.db.PersonFetchResult as PersonFetchResultCore

class PersonFetchResult(val person: Person, val fetchedOnline: Boolean) {
    companion object {
        fun fromCoreToDomain(coreFetchResult: PersonFetchResultCore) =
            PersonFetchResult(Person.fromCoreToDomain(coreFetchResult.person), coreFetchResult.fetchedOnline)
    }
}
