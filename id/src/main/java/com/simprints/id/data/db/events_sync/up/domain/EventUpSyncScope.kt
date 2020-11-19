package com.simprints.id.data.db.events_sync.up.domain

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.ProjectScope

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ProjectScope::class)
)
@Keep
abstract class EventUpSyncScope(var operation: EventUpSyncOperation) {

    @Keep
    data class ProjectScope(val projectId: String) :
        EventUpSyncScope(EventUpSyncOperation(LocalEventQuery(projectId = projectId))) {
    }
}
