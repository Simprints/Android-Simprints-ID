package com.simprints.id.data.db.project.local

import android.content.Context
import com.simprints.id.data.db.common.realm.PeopleRealmConfig
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.models.DbProject
import com.simprints.id.data.db.project.local.models.fromDbToDomain
import com.simprints.id.data.db.project.local.models.fromDomainToDb
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.id.tools.extensions.awaitFirst
import com.simprints.id.tools.extensions.transactAwait
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext

@FlowPreview
class ProjectLocalDataSourceImpl(private val appContext: Context,
                                 val secureDataManager: SecureLocalDbKeyProvider,
                                 val loginInfoManager: LoginInfoManager) : ProjectLocalDataSource {

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
        PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)


    override suspend fun load(projectId: String): Project? =
        withContext(Dispatchers.Main) {
            Realm.getInstance(config).use { realm ->
                realm.where(DbProject::class.java).equalTo(PROJECT_ID_FIELD, projectId)
                    .awaitFirst()
                    ?.let { it.fromDbToDomain() }
            }
        }


    override suspend fun save(project: Project) =
        withContext(Dispatchers.Main) {
            Realm.getInstance(config).use { realm ->
                realm.transactAwait {
                    it.insertOrUpdate(project.fromDomainToDb())
                }
            }
        }
}



