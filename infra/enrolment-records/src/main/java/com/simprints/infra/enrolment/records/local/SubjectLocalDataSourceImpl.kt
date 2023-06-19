package com.simprints.infra.enrolment.records.local

import com.simprints.infra.enrolment.records.domain.models.*
import com.simprints.infra.enrolment.records.exceptions.InvalidQueryToLoadRecordsException
import com.simprints.infra.enrolment.records.local.models.fromDbToDomain
import com.simprints.infra.enrolment.records.local.models.fromDomainToDb
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.RealmWrapper
import com.simprints.infra.realm.models.DbSubject
import io.realm.Realm
import io.realm.RealmAny
import io.realm.RealmQuery
import io.realm.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.Serializable
import java.util.*
import javax.inject.Inject

internal class SubjectLocalDataSourceImpl @Inject constructor(private val realmWrapper: RealmWrapper) :
    SubjectLocalDataSource {

    companion object {
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "attendantId"
        const val SUBJECT_ID_FIELD = "subjectId"
        const val MODULE_ID_FIELD = "moduleId"
    }

    override suspend fun load(query: SubjectQuery): Flow<Subject> =
        realmWrapper.useRealmInstance {
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

        realmWrapper.useRealmInstance { realmInstance ->
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
        realmWrapper.useRealmInstance { realm ->
            realm.buildRealmQueryForSubject(query)
                .count().toInt()
        }


    override suspend fun performActions(actions: List<SubjectAction>) {
        // if there is no actions to perform return to avoid useless realm operations
        if (actions.isEmpty()) {
            Simber.tag(CrashReportTag.REALM_DB.name)
                .d("[SubjectLocalDataSourceImpl] No realm actions to perform ")
            return
        }

        realmWrapper.useRealmInstance {
            it.executeTransaction { realm ->
                actions.forEach { action ->
                    when (action) {
                        is SubjectAction.Creation -> {
                            realm.insertOrUpdate(action.subject.fromDomainToDb())
                        }
                        is SubjectAction.Deletion -> {
                            realm.buildRealmQueryForSubject(
                                query = SubjectQuery(
                                    subjectId =
                                    action.subjectId
                                )
                            )
                                .findAll()
                                .deleteAllFromRealm()
                        }
                    }
                }
            }
        }
    }

    private fun Realm.buildRealmQueryForSubject(query: SubjectQuery): RealmQuery<DbSubject> =
        where(DbSubject::class.java)
            .apply {
                query.projectId?.let { this.equalTo(PROJECT_ID_FIELD, it) }
                query.subjectId?.let { this.equalTo(SUBJECT_ID_FIELD, UUID.fromString(it)) }
                query.subjectIds?.let { subjectIds ->
                    this.`in`(
                        SUBJECT_ID_FIELD,
                        subjectIds.map { RealmAny.valueOf(UUID.fromString(it)) }.toTypedArray()
                    )
                }
                query.attendantId?.let { this.equalTo(USER_ID_FIELD, it) }
                query.moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
                query.afterSubjectId?.let {
                    this.greaterThan(
                        SUBJECT_ID_FIELD,
                        UUID.fromString(it)
                    )
                }

                if (query.sort)
                    this.sort(SUBJECT_ID_FIELD, Sort.ASCENDING)
            }
}
