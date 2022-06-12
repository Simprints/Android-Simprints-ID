package com.simprints.id.data.db.project.local

import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.models.DbProject
import com.simprints.id.data.db.project.local.models.fromDbToDomain
import com.simprints.id.data.db.project.local.models.fromDomainToDb
import com.simprints.id.data.db.subject.local.RealmWrapper

class ProjectLocalDataSourceImpl(
    private val realmWrapper: RealmWrapper
) : ProjectLocalDataSource {

    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    override suspend fun load(projectId: String): Project? =
        realmWrapper.useRealmInstance { realm ->
            realm.where(DbProject::class.java)
                .equalTo(PROJECT_ID_FIELD, projectId)
                .findFirst()
                ?.fromDbToDomain()
        }


    override suspend fun save(project: Project) =
        realmWrapper.useRealmInstance { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(project.fromDomainToDb())
            }
        }
}



