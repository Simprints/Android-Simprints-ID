package com.simprints.eventsystem.subject

import com.simprints.eventsystem.subject.local.SubjectLocalDataSource

class SubjectRepositoryImpl(val subjectLocalDataSource: SubjectLocalDataSource) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource
