package com.simprints.id.data.db

import com.simprints.id.data.db.subject.domain.Subject

class SubjectFetchResult(val subject: Subject? = null,
                         val subjectSource: SubjectSource) {

    enum class SubjectSource {
        LOCAL,
        REMOTE,
        NOT_FOUND_IN_LOCAL_AND_REMOTE,
        NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
    }
}
