package com.simprints.infra.enrolment.records.store

import com.simprints.infra.enrolment.records.store.local.SubjectLocalDataSource
import javax.inject.Inject

internal class SubjectRepositoryImpl @Inject constructor(private val subjectLocalDataSource: SubjectLocalDataSource) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource
