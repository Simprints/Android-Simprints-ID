package com.simprints.id.commontesttools

import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.data.db.people_sync.down.domain.ModuleSyncScope
import com.simprints.id.data.db.people_sync.down.domain.ProjectSyncScope
import com.simprints.id.data.db.people_sync.down.domain.UserSyncScope
import com.simprints.id.domain.modality.Modes

object DefaultTestConstants {
    const val DEFAULT_PROJECT_ID = "vkbpRLfHvpQqaeoxZgyx"
    const val DEFAULT_MODULE_ID = "the_one_and_only_module"
    const val DEFAULT_MODULE_ID_2 = "the_one_and_only_module2"
    const val DEFAULT_USER_ID = "the_lone_user"
    const val DEFAULT_PROJECT_SECRET = "3xDCW0IL/m7nNBWPlVQljh4RzZgcho3Gp7WEj07YqgSER6ESXeY8tVczlNsxubug7co45/PsfG7JiC9oo/U54w=="

    val DEFAULT_MODES = listOf(Modes.FINGERPRINT)

    val DEFAULT_TEST_CALLOUT_CREDENTIALS = TestCalloutCredentials(
        projectId = DEFAULT_PROJECT_ID,
        moduleId = DEFAULT_MODULE_ID,
        userId = DEFAULT_USER_ID)

    val projectSyncScope = ProjectSyncScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_MODES
    )

    val userSyncScope = UserSyncScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        DEFAULT_MODES
    )

    val moduleSyncScope = ModuleSyncScope(
        DEFAULT_PROJECT_ID,
        listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
        DEFAULT_MODES
    )
}
