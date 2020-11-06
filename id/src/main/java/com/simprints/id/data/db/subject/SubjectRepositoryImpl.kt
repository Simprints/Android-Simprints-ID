package com.simprints.id.data.db.subject

import com.simprints.id.data.db.subject.local.SubjectLocalDataSource

class SubjectRepositoryImpl(val subjectLocalDataSource: SubjectLocalDataSource) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource
