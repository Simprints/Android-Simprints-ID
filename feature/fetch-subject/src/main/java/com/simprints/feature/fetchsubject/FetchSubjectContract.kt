package com.simprints.feature.fetchsubject

import com.simprints.feature.fetchsubject.screen.FetchSubjectFragmentArgs

object FetchSubjectContract {
    val DESTINATION = R.id.fetchSubjectFragment

    fun getArgs(
        projectId: String,
        subjectId: String,
    ) = FetchSubjectFragmentArgs(projectId, subjectId).toBundle()
}
