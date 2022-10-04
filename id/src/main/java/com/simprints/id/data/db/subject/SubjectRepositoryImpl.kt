package com.simprints.id.data.db.subject

import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(val subjectLocalDataSource: SubjectLocalDataSource) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource
