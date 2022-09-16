package com.simprints.id.enrolmentrecords

import android.content.Context
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.enrolmentrecords.remote.EnrolmentRecordRemoteDataSource

class EnrolmentRecordRepositoryImpl(
    context: Context,
    private val remoteDataSource: EnrolmentRecordRemoteDataSource,
    private val subjectRepository: SubjectRepository,
    private val batchSize: Int = BATCH_SIZE,
) : EnrolmentRecordRepository {

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
        if (subjects.size >= 0) {
            // The last batch
            remoteDataSource.uploadRecords(subjects)
        }
        prefs.edit().remove(PROGRESS_KEY).apply()
    }
}
