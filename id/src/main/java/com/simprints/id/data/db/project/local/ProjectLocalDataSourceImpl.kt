package com.simprints.id.data.db.project.local.models

import android.content.Context
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.data.db.NoSuchStoredProjectException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.FlowPreview

@FlowPreview
class ProjectLocalDataSourceImpl(private val appContext: Context,
                                val secureDataManager: SecureDataManager,
                                val loginInfoManager: LoginInfoManager) : ProjectLocalDataSource {

    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "userId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
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


    private fun <R> useRealmInstance(block: (Realm) -> R): R =
        Realm.getInstance(config).use(block)

    override fun load(projectId: String): Project =
        useRealmInstance { realm ->
            realm
                .where(DbProject::class.java).equalTo(DbProject.PROJECT_ID_FIELD, projectId)
                .findFirst()
                ?.let { realm.copyFromRealm(it).toDomainProject() }
                ?: throw NoSuchStoredProjectException()
        }


    override fun save(project: Project) =
        useRealmInstance { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(project.toRealmProject())
            }
        }
}



