package com.simprints.id.data.db.event.domain.events

sealed class EventQuery {

    data class SessionCaptureEventQuery(val sessionId: String? = null,
                                        val projectId: String? = null,
                                        val openSession: Boolean? = null,
                                        val startedBefore: Long? = null): EventQuery()
}
