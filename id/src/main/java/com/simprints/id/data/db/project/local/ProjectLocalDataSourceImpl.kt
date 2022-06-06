package com.simprints.id.data.db.project.local

import android.content.Context
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.security.LocalDbKey
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.models.DbProject
import com.simprints.id.data.db.project.local.models.fromDbToDomain
import com.simprints.id.data.db.project.local.models.fromDomainToDb
import com.simprints.id.data.db.subject.migration.SubjectsRealmConfig
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext

@FlowPreview
class ProjectLocalDataSourceImpl(
    private val appContext: Context,
    val secureDataManager: SecureLocalDbKeyProvider,
    val loginInfoManager: LoginInfoManager,
    private val dispatcher: DispatcherProvider
) : ProjectLocalDataSource {

    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    val config: RealmConfiguration by lazy {
        Realm.init(appContext)
        getLocalDbKeyAndCreateRealmConfig()
    }

    private fun getLocalDbKeyAndCreateRealmConfig(): RealmConfiguration =
        loginInfoManager.getSignedInProjectIdOrEmpty().let {
            return if (it.isNotEmpty()) {
                createAndSaveRealmConfig(secureDataManager.getLocalDbKeyOrThrow(it))
            } else {
                throw RealmUninitialisedException("No signed in project id found")
            }
        }

    private fun createAndSaveRealmConfig(localDbKey: LocalDbKey): RealmConfiguration =
        SubjectsRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)

    override suspend fun load(projectId: String): Project? =
        useRealmInstance { realm ->
            realm.where(DbProject::class.java)
                .equalTo(PROJECT_ID_FIELD, projectId)
                .findFirst()
                ?.fromDbToDomain()
        }


    override suspend fun save(project: Project) =
        useRealmInstance { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(project.fromDomainToDb())
            }
        }

    private suspend fun <R> useRealmInstance(block: (Realm) -> R): R =
        withContext(dispatcher.io()) { Realm.getInstance(config).use(block) }

}



