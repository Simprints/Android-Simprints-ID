package com.simprints.id.data.db.people_sync.down.domain

import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordOperationType
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordOperationType.*
import com.simprints.id.domain.modality.Modes

data class EventQuery(val projectId: String,
                      val userId: String? = null,
                      val moduleIds: List<String>? = null,
                      val subjectId: String? = null,
                      val lastEventId: String? = null,
                      val modes: List<Modes>,
                      val types: List<EnrolmentRecordOperationType>)

fun PeopleDownSyncScope.toEventQuery() = when(this) {
    is ProjectSyncScope -> {
        EventQuery(projectId, modes = modes, types = listOf(EnrolmentRecordCreation, EnrolmentRecordDeletion, EnrolmentRecordMove))
    }
    is UserSyncScope -> {
        EventQuery(projectId, userId = userId, modes = modes, types = listOf(EnrolmentRecordCreation, EnrolmentRecordDeletion, EnrolmentRecordMove))
    }
    is ModuleSyncScope -> {
        EventQuery(projectId, moduleIds = modules, modes = modes, types = listOf(EnrolmentRecordCreation, EnrolmentRecordDeletion, EnrolmentRecordMove))
    }
}
