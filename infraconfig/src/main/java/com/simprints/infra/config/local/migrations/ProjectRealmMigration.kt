package com.simprints.infra.config.local.migrations

import androidx.datastore.core.DataMigration
import com.simprints.infra.config.local.migrations.realm.DbProject
import com.simprints.infra.config.local.migrations.realm.RealmWrapperImpl
import com.simprints.infra.config.local.models.ProtoProject
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import javax.inject.Inject

/**
 * Can be removed once all the devices have been updated to 2022.3.0
 */
class ProjectRealmMigration @Inject constructor(
    private val loginManager: LoginManager,
    private val realmWrapper: RealmWrapperImpl,
) :
    DataMigration<ProtoProject> {

    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    override suspend fun cleanUp() {

    }

    override suspend fun migrate(currentData: ProtoProject): ProtoProject {
        Simber.i("Start migration of project to Datastore")
        val dbProject = load(loginManager.signedInProjectId)
        if (dbProject == null) {
            Simber.i("Got null project")
            return currentData
        }
        Simber.i("Got project from Realm with id ${dbProject.id}")
        return currentData
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

    private suspend fun load(projectId: String): DbProject? =
        realmWrapper.useRealmInstance { realm ->
            realm.where(DbProject::class.java)
                .equalTo(PROJECT_ID_FIELD, projectId)
                .findFirst()
        }
}

