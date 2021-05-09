package com.simprints.id.sampledata

import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.domain.modality.Modes
import java.util.*

object SampleDefaults {
    const val DEFAULT_DEVICE_ID = "device_id"
    const val DEFAULT_PROJECT_ID = "vkbpRLfHvpQqaeoxZgyx"
    const val DEFAULT_MODULE_ID = "the_one_and_only_module"
    const val DEFAULT_MODULE_ID_2 = "the_one_and_only_module2"
    val DEFAULT_MODULES = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)

    const val DEFAULT_USER_ID = "the_lone_user"
    const val DEFAULT_USER_ID_2 = "the_lone_user2"
    const val DEFAULT_PROJECT_SECRET = "3xDCW0IL/m7nNBWPlVQljh4RzZgcho3Gp7WEj07YqgSER6ESXeY8tVczlNsxubug7co45/PsfG7JiC9oo/U54w=="
    const val DEFAULT_METADATA = "DEFAULT_METADATA"

    const val CREATED_AT: Long = 1234L
    const val ENDED_AT: Long = 4567L
    const val DEFAULT_ENDED_AT: Long = 0L

    const val STATIC_GUID = "3f0f8e9a-0a0c-456c-846e-577b1440b6fb"
    val GUID1 = UUID.randomUUID().toString()
    val GUID2 = UUID.randomUUID().toString()
    val GUID3 = UUID.randomUUID().toString()

    val TIME1 = System.currentTimeMillis()
    val TIME2 = System.currentTimeMillis()

    val DEFAULT_MODES = listOf(Modes.FINGERPRINT)

    val projectUpSyncScope = EventUpSyncScope.ProjectScope(
        DEFAULT_PROJECT_ID
    )

    val projectDownSyncScope = EventDownSyncScope.SubjectProjectScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_MODES
    )

    val userDownSyncScope = EventDownSyncScope.SubjectUserScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        DEFAULT_MODES
    )

    val modulesDownSyncScope = EventDownSyncScope.SubjectModuleScope(
        DEFAULT_PROJECT_ID,
        listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
        DEFAULT_MODES
    )

    val defaultSubject = Subject(GUID1, DEFAULT_PROJECT_ID, GUID2, DEFAULT_MODULE_ID)
}
