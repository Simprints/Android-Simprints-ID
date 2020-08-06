package com.simprints.id.data.db.events_sync.up.domain

abstract class EventUpSyncScope(var operation: EventUpSyncOperation) {

    abstract val id: String

    data class SubjectProjectScope(val projectId: String) : EventUpSyncScope(EventUpSyncOperation(buildId(projectId), LocalEventQuery(projectId = projectId))) {
        override val id: String
            get() = buildId(projectId)

        companion object {
            private fun buildId(projectId: String) = "$projectId$separator"
        }
    }

    companion object {
        private const val separator = "||"
    }
}
