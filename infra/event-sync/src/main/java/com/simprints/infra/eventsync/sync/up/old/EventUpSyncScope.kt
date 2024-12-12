package com.simprints.infra.eventsync.sync.up.old

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope as NewEventUpSyncScope

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = EventUpSyncScope.ProjectScope::class),
)
@Keep
@Deprecated(
    message = "This is used to support old data-upload format, should be replaced with EventUpSyncScope",
    replaceWith = ReplaceWith(
        expression = "EventUpSyncScope(input)",
        imports = arrayOf("com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope"),
    ),
)
internal abstract class EventUpSyncScope(
    var operation: EventUpSyncOperation,
) {
    @Keep
    @Deprecated(
        message = "This is used to support old data-upload format, should be replaced with new ProjectScope",
        replaceWith = ReplaceWith(
            expression = "EventUpSyncScope.ProjectScope(input)",
            imports = arrayOf("com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope.ProjectScope"),
        ),
    )
    data class ProjectScope(
        val projectId: String,
    ) : EventUpSyncScope(EventUpSyncOperation(LocalEventQuery(projectId = projectId)))

    internal fun toNewScope(): NewEventUpSyncScope {
        val newScope = NewEventUpSyncScope.ProjectScope(
            operation.queryEvent.projectId ?: "",
        )

        newScope.operation.lastState = this.operation.lastState
        newScope.operation.lastSyncTime = this.operation.lastSyncTime

        return newScope
    }
}
