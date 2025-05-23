package com.simprints.feature.selectsubject

import com.simprints.feature.selectsubject.screen.SelectSubjectFragmentArgs

object SelectSubjectContract {
    val DESTINATION = R.id.selectSubjectFragment

    fun getArgs(
        projectId: String,
        subjectId: String,
        externalCredentialId: String?,
        externalCredentialImagePath: String?,
    ) = SelectSubjectFragmentArgs(
        projectId = projectId,
        subjectId = subjectId,
        externalCredentialId = externalCredentialId,
        externalCredentialImagePath = externalCredentialImagePath
    ).toBundle()
}
