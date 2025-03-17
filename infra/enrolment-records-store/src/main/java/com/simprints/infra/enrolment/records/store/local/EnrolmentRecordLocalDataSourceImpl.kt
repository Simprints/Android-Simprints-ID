package com.simprints.infra.enrolment.records.store.local

import com.simprints.core.DispatcherIO
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.store.local.models.fromDbToDomain
import com.simprints.infra.enrolment.records.store.local.models.fromDomainToDb
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB
import com.simprints.infra.logging.Simber
import com.simprints.infra.realm.ObjectboxWrapper
import com.simprints.infra.realm.models.DbFaceSample
import com.simprints.infra.realm.models.DbFaceSample_
import com.simprints.infra.realm.models.DbSubject
import com.simprints.infra.realm.models.DbSubject_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

internal class EnrolmentRecordLocalDataSourceImpl @Inject constructor(
    private val realmWrapper: ObjectboxWrapper,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
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

    override suspend fun load(query: SubjectQuery): List<Subject> = withContext(dispatcher) {
        realmWrapper.readObjectBox { store ->
            var queryBuilder = store.boxFor<DbSubject>().buildBoxQueryForSubject(query)
            log("load: $queryBuilder")
            // / log the time
            var result = emptyList<Subject>()
            val timeTaken = measureTimeMillis {
                result = queryBuilder.find().map { it.fromDbToDomain() }
            }
            log("load ${result.size} time: ${timeTaken}ms")
            result
        }
    }

    override suspend fun loadFingerprintIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FingerprintIdentity> = withContext(dispatcher) {
        realmWrapper.readObjectBox { store ->
            var queryBuilder = store.boxFor<DbSubject>().buildBoxQueryForSubject(query)

            queryBuilder.find(range.first.toLong(), range.last.toLong()).map {
                FingerprintIdentity(
                    it.subjectUuid,
                    it.fromDbToDomain().fingerprintSamples,
                )
            }
        }
    }

    override suspend fun loadFaceIdentities(
        query: SubjectQuery,
        range: IntRange,
        dataSource: BiometricDataSource,
    ): List<FaceIdentity> = withContext(dispatcher) {
        realmWrapper.readObjectBox { store ->
            var queryBuilder = store.boxFor<DbSubject>().buildBoxQueryForSubject(query)
            var result = emptyList<FaceIdentity>()
            val timeTaken = measureTimeMillis {
                result = queryBuilder.find(range.first.toLong(), range.last.toLong()).map {
                    FaceIdentity(
                        it.subjectUuid,
                        it.fromDbToDomain().faceSamples,
                    )
                }
            }
            log("loadFaceIdentities ${result.size} time: ${timeTaken}ms")
            result
        }
    }

    override suspend fun delete(queries: List<SubjectQuery>) {
    }

    override suspend fun deleteAll() {
    }

    override suspend fun count(
        query: SubjectQuery,
        dataSource: BiometricDataSource,
    ): Int = realmWrapper.readObjectBox { store ->

        var queryBuilder = store.boxFor<DbSubject>().buildBoxQueryForSubject(query)
        var count: Int
        val timeTaken = measureTimeMillis {
            count = queryBuilder.count().toInt()
        }

        log("count: $count, time: ${timeTaken}ms")
        count
    }

    override suspend fun getNearestNeighbour(facesample: FloatArray): List<Pair<String, Float>> = realmWrapper.readObjectBox { store ->
        val result = measureTimedValue {
            val query = store.boxFor<DbFaceSample>().query(DbFaceSample_.template.nearestNeighbors(facesample, 10)).build()
            val result = query.findWithScores()
            result.map {
                Pair(it.get().subjectId, it.score.toFloat())
            }
        }
        log("getNearestNeighbour time: ${result.duration.inWholeMilliseconds}")
        result.value
    }

    override suspend fun performActions(actions: List<SubjectAction>): Unit = withContext(dispatcher) {
        // if there is no actions to perform return to avoid useless realm operations
        if (actions.isEmpty()) {
            Simber.d("[SubjectLocalDataSourceImpl] No realm actions to perform", tag = REALM_DB)
            return@withContext
        }
        val timeTaken = measureTimeMillis {
            realmWrapper.writeObjectBox { store ->
                val box = store.boxFor<DbSubject>()
                actions.forEach { action ->

                    when (action) {
                        is SubjectAction.Creation -> {
                            val subject = action.subject.fromDomainToDb()
                            try {
                                // put face samples first
                                val dbfaceSamples = action.subject.faceSamples.map { it.fromDomainToDb(subject.subjectUuid) }
                                store.boxFor<DbFaceSample>().put(dbfaceSamples)
                                // put the subject
                                box.put(subject)
                            } catch (e: Exception) {
                                Simber.e("Error creating subject ${action.subject.subjectId}", e)
                            }
                        }

                        is SubjectAction.Deletion -> box.remove(
                            box
                                .buildBoxQueryForSubject(
                                    query = SubjectQuery(
                                        subjectId = action.subjectId,
                                    ),
                                ).find(),
                        )
                    }
                }
            }
        }
        log("performActions ${actions.size} time: ${timeTaken}ms")
    }

    private fun Box<DbSubject>.buildBoxQueryForSubject(query: SubjectQuery): Query<DbSubject> {
        var queryBuilder = query()
        // add any non null query parameters
        if (query.projectId != null) {
            queryBuilder.apply(
                DbSubject_.projectId.equal(query.projectId),
            )
        }
        if (query.subjectId != null) {
            queryBuilder.apply(
                DbSubject_.subjectUuid.equal(query.subjectId),
            )
        }
        if (query.subjectIds != null) {
            queryBuilder.apply(
                DbSubject_.subjectUuid.`in`(query.subjectIds),
            )
        }
        if (query.attendantId != null) {
            queryBuilder.apply(
                DbSubject_.attendantId.equal(query.attendantId),
            )
        }
        if (query.moduleId != null) {
            queryBuilder.apply(
                DbSubject_.moduleId.equal(query.moduleId),
            )
        }

        if (query.afterSubjectId != null) {
//            queryBuilder.apply(
//                DbSubject_.subjectUuid.greaterThan(query.afterSubjectId),
//            )
            // Todo
        }
        if (query.hasUntokenizedFields != null) {
            queryBuilder.apply(
                DbSubject_.isAttendantIdTokenized.equal(false),
            )
            queryBuilder.apply(
                DbSubject_.isModuleIdTokenized.equal(false),
            )
        }
        if (query.sort) {
            queryBuilder.order(DbSubject_.subjectUuid)
        }
        return queryBuilder.build()
    }
}

fun log(message: String) {
    Simber.i(message, tag = "objectbox")
}
