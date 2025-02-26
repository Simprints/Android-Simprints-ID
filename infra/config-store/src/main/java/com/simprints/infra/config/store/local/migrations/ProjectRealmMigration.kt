package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbProject
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.4.0
 */
internal class ProjectRealmMigration @Inject constructor(
    private val authStore: AuthStore,
    private val realmWrapper: RealmWrapper,
) : DataMigration<ProtoProject> {
    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    override suspend fun cleanUp() {
        Simber.i("Migration of project to Datastore done", tag = MIGRATION)
        realmWrapper.writeRealm { realm ->
            realm.delete(DbProject::class)
        }
    }

    override suspend fun migrate(currentData: ProtoProject): ProtoProject {
        Simber.i("Start migration of project to Datastore", tag = MIGRATION)
        val dbProject = realmWrapper.readRealm {
            it
                .query(DbProject::class, "$PROJECT_ID_FIELD == $0", authStore.signedInProjectId)
                .first()
                .find()
        } ?: return currentData

        return currentData
            .toBuilder()
            .setId(dbProject.id)
            .setName(dbProject.name)
            .setCreator(dbProject.creator)
            .setDescription(dbProject.description)
            .setImageBucket(dbProject.imageBucket)
            .build()
    }

    override suspend fun shouldMigrate(currentData: ProtoProject): Boolean =
        authStore.signedInProjectId.isNotEmpty() && currentData.id.isEmpty()
}
