package com.simprints.infra.config.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.local.models.ProtoProject
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.realm.RealmWrapper
import com.simprints.infra.realm.models.DbProject
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.3.0
 */
class ProjectRealmMigration @Inject constructor(
    private val loginManager: LoginManager,
    private val realmWrapper: RealmWrapper,
) :
    DataMigration<ProtoProject> {

    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    override suspend fun cleanUp() {
        Simber.i("Migration of project to Datastore done")
        realmWrapper.useRealmInstance { realm ->
            realm.beginTransaction()
            realm.delete(DbProject::class.java)
            realm.commitTransaction()
        }
    }

    override suspend fun migrate(currentData: ProtoProject): ProtoProject =
        realmWrapper.useRealmInstance { realm ->
            Simber.i("Start migration of project to Datastore")
            val dbProject = realm
                .where(DbProject::class.java)
                .equalTo(PROJECT_ID_FIELD, loginManager.signedInProjectId)
                .findFirst() ?: return@useRealmInstance currentData

            currentData
                .toBuilder()
                .setId(dbProject.id)
                .setName(dbProject.name)
                .setCreator(dbProject.creator)
                .setDescription(dbProject.description)
                .setImageBucket(dbProject.imageBucket)
                .build()
        }

    override suspend fun shouldMigrate(currentData: ProtoProject): Boolean {
        return loginManager.signedInProjectId.isNotEmpty() && currentData.id.isEmpty()
    }
}

