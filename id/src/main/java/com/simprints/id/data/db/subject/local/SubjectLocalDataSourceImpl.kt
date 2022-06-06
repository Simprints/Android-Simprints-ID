package com.simprints.id.data.db.subject.local

import android.content.Context
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.security.LocalDbKey
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.db.subject.domain.FaceIdentity
import com.simprints.id.data.db.subject.domain.FingerprintIdentity
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.SubjectAction
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.domain.SubjectAction.Deletion
import com.simprints.id.data.db.subject.local.models.DbSubject
import com.simprints.id.data.db.subject.local.models.fromDbToDomain
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import com.simprints.id.data.db.subject.migration.SubjectsRealmConfig
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import com.simprints.id.exceptions.unexpected.RealmUninitialisedException
import com.simprints.logging.Simber
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.Serializable

class SubjectLocalDataSourceImpl(
    private val appContext: Context,
    val secureDataManager: SecureLocalDbKeyProvider,
    val loginInfoManager: LoginInfoManager,
    private val dispatcher: DispatcherProvider
) : SubjectLocalDataSource {

    companion object {
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "attendantId"
        const val SUBJECT_ID_FIELD = "subjectId"
        const val MODULE_ID_FIELD = "moduleId"
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

    override suspend fun load(query: SubjectQuery?): Flow<Subject> =
       useRealmInstance {
            it.buildRealmQueryForSubject(query)
                .findAll()
                ?.map { dbSubject -> dbSubject.fromDbToDomain() }
                ?.asFlow()
                ?: flowOf()
        }


    override suspend fun loadFingerprintIdentities(query: Serializable): Flow<FingerprintIdentity> =
        if (query is SubjectQuery) {
            load(query).map { subject ->
                FingerprintIdentity(subject.subjectId, subject.fingerprintSamples)
            }
        } else {
            throw InvalidQueryToLoadRecordsException()
        }

    override suspend fun loadFaceIdentities(query: Serializable): Flow<FaceIdentity> =
        if (query is SubjectQuery) {
            load(query).map { subject ->
                FaceIdentity(subject.subjectId, subject.faceSamples)
            }
        } else {
            throw InvalidQueryToLoadRecordsException()
        }

    override suspend fun delete(queries: List<SubjectQuery>) {

        useRealmInstance { realmInstance ->
            realmInstance.executeTransaction { realm ->
                queries.forEach {
                    realm.buildRealmQueryForSubject(it)
                        .findAll()
                        .deleteAllFromRealm()
                }
            }
        }

    }

    override suspend fun deleteAll() {
        delete(listOf(SubjectQuery()))
    }

    override suspend fun count(query: SubjectQuery): Int =
        useRealmInstance { realm ->
            realm.buildRealmQueryForSubject(query)
                .count().toInt()
        }


    override suspend fun performActions(actions: List<SubjectAction>) {
        // if there is no actions to perform return to avoid useless realm operations
        if (actions.isEmpty()) {
            Simber.d("No actions to perform ")
            return
        }

        useRealmInstance {
            it.executeTransaction { realm ->
                actions.forEach { action ->
                    when (action) {
                        is Creation -> {
                            realm.insertOrUpdate(action.subject.fromDomainToDb())
                        }
                        is Deletion -> {
                            realm.buildRealmQueryForSubject(query = SubjectQuery(subjectId = action.subjectId))
                                .findAll()
                                .deleteAllFromRealm()
                        }
                    }
                }
            }
        }
    }

    private fun Realm.buildRealmQueryForSubject(query: SubjectQuery?): RealmQuery<DbSubject> =
        where(DbSubject::class.java)
            .apply {
                query?.let { query ->
                    query.projectId?.let { this.equalTo(PROJECT_ID_FIELD, it) }
                    query.subjectId?.let { this.equalTo(SUBJECT_ID_FIELD, it) }
                    query.attendantId?.let { this.equalTo(USER_ID_FIELD, it) }
                    query.moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
                }
            }

    private suspend fun <R> useRealmInstance(block: (Realm) -> R): R =
        withContext(dispatcher.io()) { Realm.getInstance(config).use(block) }

}
