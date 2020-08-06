package com.simprints.id.data.db.events_sync.up.domain

abstract class EventUpSyncScope {

    abstract val id: String

    data class SubjectProjectScope(val projectId: String) : EventUpSyncScope() {
        override val id: String
            get() = "$projectId$separator"
    }

    companion object {
        private const val separator = "||"
    }
}
