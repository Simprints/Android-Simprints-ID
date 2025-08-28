package com.simprints.feature.fetchsubject

object FetchSubjectContract {
    val DESTINATION = R.id.fetchSubjectFragment

    fun getParams(
        projectId: String,
        subjectId: String,
        metadata: String,
    ) = FetchSubjectParams(projectId, subjectId, metadata)
}
