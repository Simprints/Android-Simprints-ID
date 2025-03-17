package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.ObjectboxWrapper
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.4.0
 */
internal class ProjectRealmMigration @Inject constructor(
    private val authStore: AuthStore,
    private val realmWrapper: ObjectboxWrapper,
) : DataMigration<ProtoProject> {
    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    override suspend fun cleanUp() {
        Simber.i("Migration of project to Datastore done", tag = MIGRATION)
    }

    override suspend fun migrate(currentData: ProtoProject): ProtoProject {
        Simber.i("Start migration of project to Datastore", tag = MIGRATION)
        return currentData
    }

    override suspend fun shouldMigrate(currentData: ProtoProject): Boolean =
        authStore.signedInProjectId.isNotEmpty() && currentData.id.isEmpty()
}
