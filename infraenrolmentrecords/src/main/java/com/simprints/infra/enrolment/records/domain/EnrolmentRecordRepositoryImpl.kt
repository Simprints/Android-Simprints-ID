package com.simprints.infra.enrolment.records.domain

import android.content.Context
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.remote.EnrolmentRecordRemoteDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class EnrolmentRecordRepositoryImpl(
    context: Context,
    private val remoteDataSource: EnrolmentRecordRemoteDataSource,
    private val subjectRepository: SubjectRepository,
    private val batchSize: Int,
) : EnrolmentRecordRepository {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        remoteDataSource: EnrolmentRecordRemoteDataSource,
        subjectRepository: SubjectRepository
    ) : this(context, remoteDataSource, subjectRepository, BATCH_SIZE)

    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val BATCH_SIZE = 80
        private const val PREF_FILE_NAME = "UPLOAD_ENROLMENT_RECORDS_PROGRESS"
        private const val PROGRESS_KEY = "PROGRESS"
    }

    override suspend fun uploadRecords(subjectIds: List<String>) {
        val lastUploadedRecord = prefs.getString(PROGRESS_KEY, null)
        var query = SubjectQuery(sort = true, afterSubjectId = lastUploadedRecord)
        if (subjectIds.isNotEmpty()) {
            query = SubjectQuery(
                subjectIds = subjectIds,
                sort = true,
                afterSubjectId = lastUploadedRecord
            )
        }
        var subjects = mutableListOf<Subject>()
        subjectRepository.load(query).collect {
            subjects.add(it)
            if (subjects.size >= this.batchSize) {
                remoteDataSource.uploadRecords(subjects)
                prefs.edit().putString(PROGRESS_KEY, subjects.last().subjectId).apply()
                subjects = mutableListOf()
            }
        }
        if (subjects.isNotEmpty()) {
            // The last batch
            remoteDataSource.uploadRecords(subjects)
        }
        prefs.edit().remove(PROGRESS_KEY).apply()
    }
}
