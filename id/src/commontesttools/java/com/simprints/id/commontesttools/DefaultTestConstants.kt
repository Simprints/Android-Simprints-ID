package com.simprints.id.commontesttools

import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.id.domain.modality.Modes
import java.util.*

object DefaultTestConstants {
    const val DEFAULT_PROJECT_ID = "vkbpRLfHvpQqaeoxZgyx"
    const val DEFAULT_MODULE_ID = "the_one_and_only_module"
    const val DEFAULT_MODULE_ID_2 = "the_one_and_only_module2"
    val DEFAULT_MODULES = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)

    const val DEFAULT_USER_ID = "the_lone_user"
    const val DEFAULT_USER_ID_2 = "the_lone_user2"
    const val DEFAULT_PROJECT_SECRET = "3xDCW0IL/m7nNBWPlVQljh4RzZgcho3Gp7WEj07YqgSER6ESXeY8tVczlNsxubug7co45/PsfG7JiC9oo/U54w=="
    const val DEFAULT_METADATA = "DEFAULT_METADATA"

    val GUID1 = UUID.randomUUID().toString()
    val GUID2 = UUID.randomUUID().toString()
    val GUID3 = UUID.randomUUID().toString()

    val TIME1 = System.currentTimeMillis()
    val TIME2 = System.currentTimeMillis()

    val DEFAULT_MODES = listOf(Modes.FINGERPRINT)

    val DEFAULT_TEST_CALLOUT_CREDENTIALS = TestCalloutCredentials(
        projectId = DEFAULT_PROJECT_ID,
        moduleId = DEFAULT_MODULE_ID,
        userId = DEFAULT_USER_ID)

    val projectSyncScope = ProjectScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_MODES
    )

    val userSyncScope = UserScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        DEFAULT_MODES
    )

    val modulesSyncScope = ModuleScope(
        DEFAULT_PROJECT_ID,
        listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
        DEFAULT_MODES
    )
}
