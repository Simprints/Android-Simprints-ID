package com.simprints.infra.eventsync

import com.simprints.core.domain.tokenization.values
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope

internal object SampleSyncScopes {
    val projectUpSyncScope = EventUpSyncScope.ProjectScope(
        DEFAULT_PROJECT_ID,
    )

    val projectDownSyncScope = EventDownSyncScope.SubjectProjectScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_MODES,
    )

    val userDownSyncScope = EventDownSyncScope.SubjectUserScope(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID.value,
        DEFAULT_MODES,
    )

    val modulesDownSyncScope = EventDownSyncScope.SubjectModuleScope(
        DEFAULT_PROJECT_ID,
        listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2).values(),
        DEFAULT_MODES,
    )
}
