package com.simprints.id.data.db.subject

import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.services.sync.subjects.up.controllers.SubjectsUpSyncExecutor

class SubjectRepositoryImpl(private val eventRemoteDataSource: EventRemoteDataSource,
                            val subjectLocalDataSource: SubjectLocalDataSource,
                            private val peopleUpSyncExecutor: SubjectsUpSyncExecutor) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource,
    EventRemoteDataSource by eventRemoteDataSource {


    override suspend fun saveAndUpload(subject: Subject) {
        subjectLocalDataSource.insertOrUpdate(listOf(subject.apply { toSync = true }))
        peopleUpSyncExecutor.sync()
    }

    override suspend fun save(subject: Subject) {
        subjectLocalDataSource.insertOrUpdate(listOf(subject))
    }
}
