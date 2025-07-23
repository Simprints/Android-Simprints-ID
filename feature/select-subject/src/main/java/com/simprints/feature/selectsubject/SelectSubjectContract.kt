package com.simprints.feature.selectsubject

object SelectSubjectContract {
    val DESTINATION = R.id.selectSubjectFragment

    fun getParams(
        projectId: String,
        subjectId: String,
    ) = SelectSubjectParams(projectId, subjectId)
}
