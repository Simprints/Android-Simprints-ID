package com.simprints.feature.fetchsubject

import com.simprints.feature.fetchsubject.screen.FetchSubjectFragmentArgs

object FetchSubjectContract {

    const val FETCH_SUBJECT_RESULT = "fetch_subject_result"

    fun getArgs(projectId: String, subjectId: String) =
        FetchSubjectFragmentArgs(projectId, subjectId).toBundle()
}
