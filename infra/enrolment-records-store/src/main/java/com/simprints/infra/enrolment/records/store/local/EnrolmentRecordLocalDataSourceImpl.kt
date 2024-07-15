package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.store.local.models.fromDbToDomain
import com.simprints.infra.enrolment.records.store.local.models.fromDomainToDb
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.RealmWrapper
import com.simprints.infra.realm.models.DbFaceSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.infra.realm.models.DbSubject
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.find
import io.realm.kotlin.types.RealmUUID
import javax.inject.Inject

internal class EnrolmentRecordLocalDataSourceImpl @Inject constructor(
    private val realmWrapper: RealmWrapper,
) : EnrolmentRecordLocalDataSource {

    companion object {

        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "attendantId"
        const val SUBJECT_ID_FIELD = "subjectId"
        const val MODULE_ID_FIELD = "moduleId"
        const val IS_ATTENDANT_ID_TOKENIZED_FIELD = "isAttendantIdTokenized"
        const val IS_MODULE_ID_TOKENIZED_FIELD = "isModuleIdTokenized"
        const val FINGERPRINT_SAMPLES_FIELD = "fingerprintSamples"
        const val FACE_SAMPLES_FIELD = "faceSamples"
        const val FORMAT_FIELD = "format"
    }

    override suspend fun load(query: SubjectQuery): List<Subject> = realmWrapper.readRealm {
        it.query(DbSubject::class).buildRealmQueryForSubject(query)
            .find()
            .map { dbSubject -> dbSubject.fromDbToDomain() }
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FingerprintIdentity> = realmWrapper.readRealm { realm ->
        realm.query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .find { it.subList(range.first, range.last) }
            .map { subject ->
                FingerprintIdentity(
                    subject.subjectId.toString(),
                    subject.fingerprintSamples.map(DbFingerprintSample::fromDbToDomain)
                )
            }
    }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FaceIdentity> = realmWrapper.readRealm { realm ->
        realm.query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .find { it.subList(range.first, range.last) }
            .map { subject ->
                FaceIdentity(
                    subject.subjectId.toString(),
                    subject.faceSamples.map(DbFaceSample::fromDbToDomain)
                )
            }
    }

    override suspend fun delete(queries: List<SubjectQuery>) {
        realmWrapper.writeRealm { realm ->
            queries.forEach {
                realm.delete(realm.query(DbSubject::class).buildRealmQueryForSubject(it))
            }
        }
    }

    override suspend fun deleteAll() {
        realmWrapper.writeRealm { realm ->
            realm.deleteAll()
        }
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = realmWrapper.readRealm { realm ->
        realm.query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .count()
            .find()
            .toInt()
    }


    override suspend fun performActions(actions: List<SubjectAction>) {
        // if there is no actions to perform return to avoid useless realm operations
        if (actions.isEmpty()) {
            Simber.tag(CrashReportTag.REALM_DB.name)
                .d("[SubjectLocalDataSourceImpl] No realm actions to perform ")
            return
        }

        realmWrapper.writeRealm { realm ->
            actions.forEach { action ->
                when (action) {
                    is SubjectAction.Creation -> realm.copyToRealm(
                        action.subject.fromDomainToDb(),
                        updatePolicy = UpdatePolicy.ALL
                    )

                    is SubjectAction.Deletion -> realm.delete(
                        realm.query(DbSubject::class).buildRealmQueryForSubject(
                            query = SubjectQuery(
                                subjectId =
                                action.subjectId
                            )
                        ).find()
                    )
                }
            }
        }
    }

    private fun RealmQuery<DbSubject>.buildRealmQueryForSubject(
        query: SubjectQuery,
    ): RealmQuery<DbSubject> {
        var realmQuery = this

        if (query.projectId != null) {
            realmQuery = realmQuery.query("$PROJECT_ID_FIELD == $0", query.projectId)
        }
        if (query.subjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD == $0",
                RealmUUID.from(query.subjectId)
            )
        }
        if (query.subjectIds != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD IN $0",
                query.subjectIds.map { RealmUUID.from(it) }
            )
        }
        if (query.attendantId != null) {
            realmQuery = realmQuery.query("$USER_ID_FIELD == $0", query.attendantId)
        }
        if (query.moduleId != null) {
            realmQuery = realmQuery.query("$MODULE_ID_FIELD == $0", query.moduleId)
        }
        if (query.fingerprintSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY $FINGERPRINT_SAMPLES_FIELD.$FORMAT_FIELD == $0",
                query.fingerprintSampleFormat
            )
        }
        if (query.faceSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY $FACE_SAMPLES_FIELD.$FORMAT_FIELD == $0",
                query.faceSampleFormat
            )
        }
        if (query.afterSubjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD >= $0",
                RealmUUID.from(query.afterSubjectId)
            )
        }
        if (query.hasUntokenizedFields != null) {
            realmQuery = realmQuery.query(
                "$IS_ATTENDANT_ID_TOKENIZED_FIELD == $0 OR $IS_MODULE_ID_TOKENIZED_FIELD == $1",
                false,
                false
            )
        }
        if (query.sort) {
            realmQuery = realmQuery.sort(SUBJECT_ID_FIELD, Sort.ASCENDING)
        }
        return realmQuery
    }
}
