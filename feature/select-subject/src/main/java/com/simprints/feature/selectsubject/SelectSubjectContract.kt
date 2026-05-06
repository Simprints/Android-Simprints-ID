package com.simprints.feature.selectsubject

import com.simprints.feature.externalcredential.ExternalCredentialSearchResult

object SelectSubjectContract {
    val DESTINATION = R.id.selectSubjectFragment

    fun getParams(
        projectId: String,
        subjectId: String,
        scannedCredentialResult: ExternalCredentialSearchResult.Complete?,
    ) = SelectSubjectParams(projectId, subjectId, scannedCredentialResult)
}
