package com.simprints.id.data.db.subject

import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource

class SubjectRepositoryImpl(private val eventRemoteDataSource: EventRemoteDataSource,
                            val subjectLocalDataSource: SubjectLocalDataSource) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource,
    EventRemoteDataSource by eventRemoteDataSource {


    override suspend fun save(subject: Subject) {
        subjectLocalDataSource.insertOrUpdate(listOf(subject))
    }
}
