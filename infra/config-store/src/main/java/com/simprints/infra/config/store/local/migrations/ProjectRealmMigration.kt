package com.simprints.infra.config.store.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.RealmWrapper
import com.simprints.infra.realm.models.DbProject
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
        Simber.i("Migration of project to Datastore done")
        realmWrapper.writeRealm { realm ->
            realm.delete(DbProject::class)
        }
    }

    override suspend fun migrate(currentData: ProtoProject): ProtoProject {
        Simber.i("Start migration of project to Datastore")
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
