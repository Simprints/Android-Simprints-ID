package com.simprints.eventsystem

import com.simprints.eventsystem.subject.domain.Subject

data class SubjectFetchResult(val subject: Subject? = null,
                              val subjectSource: SubjectSource) {

    enum class SubjectSource {
        LOCAL,
        REMOTE,
        NOT_FOUND_IN_LOCAL_AND_REMOTE,
        NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
    }
}
