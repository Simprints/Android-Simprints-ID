package com.simprints.feature.selectsubject

import com.simprints.feature.selectsubject.screen.SelectSubjectFragmentArgs

object SelectSubjectContract {
    val DESTINATION = R.id.selectSubjectFragment

    fun getArgs(
        projectId: String,
        subjectId: String,
    ) = SelectSubjectFragmentArgs(projectId, subjectId).toBundle()
}
