package com.simprints.id.data.db.events_sync.down.domain

import com.simprints.id.domain.modality.Modes

abstract class EventDownSyncScope(val operations: MutableList<EventDownSyncOperation> = mutableListOf()) {

    abstract val id: String

    data class SubjectProjectScope(val projectId: String,
                                   val modes: List<Modes>) : EventDownSyncScope() {
        override val id: String
            get() = "$projectId$separator" +
                modes.joinToString(separator)
    }

    data class SubjectUserScope(val projectId: String,
                                val attendantId: String,
                                val modes: List<Modes>) : EventDownSyncScope() {
        override val id: String
            get() =
                "$projectId$separator" +
                    "$attendantId$separator" +
                    modes.joinToString(separator)
    }

    data class SubjectModuleScope(val projectId: String,
                                  val moduleIds: List<String>,
                                  val modes: List<Modes>) : EventDownSyncScope() {
        override val id: String
            get() =
                "$projectId$separator" +
                    "${moduleIds.joinToString(separator)}$separator" +
                    modes.joinToString(separator)

    }

    companion object {
        private const val separator = "||"
    }
}
