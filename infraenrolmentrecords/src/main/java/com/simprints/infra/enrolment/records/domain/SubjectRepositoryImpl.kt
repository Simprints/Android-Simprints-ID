package com.simprints.infra.enrolment.records.domain

import com.simprints.infra.enrolment.records.local.SubjectLocalDataSource
import javax.inject.Inject

internal class SubjectRepositoryImpl @Inject constructor(private val subjectLocalDataSource: SubjectLocalDataSource) :
    SubjectRepository,
    SubjectLocalDataSource by subjectLocalDataSource
