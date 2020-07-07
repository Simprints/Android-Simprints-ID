package com.simprints.id.data.db.event.domain.events

data class SessionQuery(val id: String? = null,
                        val projectId: String? = null,
                        val openSession: Boolean? = null,
                        val startedBefore: Long? = null)
