package com.simprints.id.data.db

import com.simprints.id.data.db.person.domain.Person

class PersonFetchResult(val person: Person? = null,
                        val personSource: PersonSource) {

    enum class PersonSource {
        LOCAL,
        REMOTE,
        NOT_FOUND_IN_LOCAL_AND_REMOTE,
        NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
    }
}
