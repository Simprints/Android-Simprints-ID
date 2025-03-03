package com.simprints.infra.enrolment.records.repository.local

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.realm.store.RealmWrapper
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.models.fromDbToDomain
import com.simprints.infra.enrolment.records.repository.local.models.fromDomainToDb
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB
import com.simprints.infra.logging.Simber
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.find
import io.realm.kotlin.types.RealmUUID
import javax.inject.Inject

internal class EnrolmentRecordLocalDataSourceImpl @Inject constructor(
    private val realmWrapper: RealmWrapper,
    private val tokenizationProcessor: TokenizationProcessor,
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
        it
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .find()
            .map { dbSubject -> dbSubject.fromDbToDomain() }
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ): List<FingerprintIdentity> = realmWrapper.readRealm { realm ->
        realm
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .find { it.subList(range.first, range.last) }
            .map { subject ->
                onCandidateLoaded()
                FingerprintIdentity(
                    subject.subjectId.toString(),
                    subject.fingerprintSamples.map(DbFingerprintSample::fromDbToDomain),
                )
            }
    }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
        project: Project,
        onCandidateLoaded: () -> Unit,
    ): List<FaceIdentity> = realmWrapper.readRealm { realm ->
        realm
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .find { it.subList(range.first, range.last) }
            .map { subject ->
                onCandidateLoaded()
                FaceIdentity(
                    subject.subjectId.toString(),
                    subject.faceSamples.map(DbFaceSample::fromDbToDomain),
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
        realm
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .count()
            .find()
            .toInt()
    }

    override suspend fun performActions(
        actions: List<SubjectAction>,
        project: Project,
    ) {
        // if there is no actions to perform return to avoid useless realm operations
        if (actions.isEmpty()) {
            Simber.d("[SubjectLocalDataSourceImpl] No realm actions to perform", tag = REALM_DB)
            return
        }

        realmWrapper.writeRealm { realm ->
            actions.forEach { action ->
                when (action) {
                    is SubjectAction.Creation -> {
                        val newSubject = action.subject
                            .copy(
                                moduleId = action.subject.moduleId.tokenizeIfNecessary(TokenKeyType.ModuleId, project),
                                attendantId = action.subject.attendantId.tokenizeIfNecessary(TokenKeyType.AttendantId, project),
                            ).fromDomainToDb()
                        val dbSubject: DbSubject? = realm.findSubject(newSubject.subjectId)

                        if (dbSubject != null) {
                            // When updating an existing subject, we must manually delete outdated samples
                            val fingerprintSampleIds = newSubject.fingerprintSamples.map { it.id }.toSet()
                            dbSubject.fingerprintSamples
                                .filterNot { it.id in fingerprintSampleIds }
                                .takeIf { it.isNotEmpty() }
                                ?.forEach { realm.delete(it) }

                            val faceSampleIds = newSubject.faceSamples.map { it.id }.toSet()
                            dbSubject.faceSamples
                                .filterNot { it.id in faceSampleIds }
                                .takeIf { it.isNotEmpty() }
                                ?.forEach { realm.delete(it) }
                        }

                        realm.copyToRealm(newSubject, updatePolicy = UpdatePolicy.ALL)
                    }

                    is SubjectAction.Update -> {
                        val dbSubject: DbSubject? = realm.findSubject(RealmUUID.from(action.subjectId))
                        if (dbSubject != null) {
                            val referencesToDelete = action.referenceIdsToRemove.toSet() // to make lookup O(1)
                            val faceSamplesMap = dbSubject.faceSamples.groupBy { it.referenceId in referencesToDelete }
                            val fingerprintSamplesMap = dbSubject.fingerprintSamples.groupBy { it.referenceId in referencesToDelete }

                            // Append new samples to the list of samples that remain after removing
                            dbSubject.faceSamples = (
                                faceSamplesMap[false].orEmpty() + action.faceSamplesToAdd.map { it.fromDomainToDb() }
                            ).toRealmList()
                            dbSubject.fingerprintSamples = (
                                fingerprintSamplesMap[false].orEmpty() + action.fingerprintSamplesToAdd.map { it.fromDomainToDb() }
                            ).toRealmList()

                            faceSamplesMap[true]?.forEach { realm.delete(it) }
                            fingerprintSamplesMap[true]?.forEach { realm.delete(it) }
                            realm.copyToRealm(dbSubject, updatePolicy = UpdatePolicy.ALL)
                        } else {
                            Simber.i("[SubjectLocalDataSourceImpl] Subject not found for update", tag = REALM_DB)
                        }
                    }

                    is SubjectAction.Deletion -> realm.delete(
                        realm
                            .query(DbSubject::class)
                            .buildRealmQueryForSubject(query = SubjectQuery(subjectId = action.subjectId))
                            .find(),
                    )
                }
            }
        }
    }

    private fun TokenizableString.tokenizeIfNecessary(
        tokenKeyType: TokenKeyType,
        project: Project,
    ) = when (this) {
        is TokenizableString.Raw -> tokenizationProcessor.encrypt(
            decrypted = this,
            tokenKeyType = tokenKeyType,
            project = project,
        )

        is TokenizableString.Tokenized -> this
    }

    private fun MutableRealm.findSubject(subjectId: RealmUUID): DbSubject? =
        query(DbSubject::class).query("$SUBJECT_ID_FIELD == $0", subjectId).first().find()

    private fun RealmQuery<DbSubject>.buildRealmQueryForSubject(query: SubjectQuery): RealmQuery<DbSubject> {
        var realmQuery = this

        if (query.projectId != null) {
            realmQuery = realmQuery.query("$PROJECT_ID_FIELD == $0", query.projectId)
        }
        if (query.subjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD == $0",
                RealmUUID.from(query.subjectId),
            )
        }
        if (query.subjectIds != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD IN $0",
                query.subjectIds.map { RealmUUID.from(it) },
            )
        }
        if (query.attendantId != null) {
            realmQuery = realmQuery.query("$USER_ID_FIELD == $0", query.attendantId.value)
        }
        if (query.moduleId != null) {
            realmQuery = realmQuery.query("$MODULE_ID_FIELD == $0", query.moduleId.value)
        }
        if (query.fingerprintSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY $FINGERPRINT_SAMPLES_FIELD.$FORMAT_FIELD == $0",
                query.fingerprintSampleFormat,
            )
        }
        if (query.faceSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY $FACE_SAMPLES_FIELD.$FORMAT_FIELD == $0",
                query.faceSampleFormat,
            )
        }
        if (query.afterSubjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD >= $0",
                RealmUUID.from(query.afterSubjectId),
            )
        }
        if (query.hasUntokenizedFields != null) {
            realmQuery = realmQuery.query(
                "$IS_ATTENDANT_ID_TOKENIZED_FIELD == $0 OR $IS_MODULE_ID_TOKENIZED_FIELD == $1",
                false,
                false,
            )
        }
        if (query.sort) {
            realmQuery = realmQuery.sort(SUBJECT_ID_FIELD, Sort.ASCENDING)
        }
        return realmQuery
    }
}
