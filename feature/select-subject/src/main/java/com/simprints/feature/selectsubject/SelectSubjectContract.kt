package com.simprints.feature.selectsubject

import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential

object SelectSubjectContract {
    val DESTINATION = R.id.selectSubjectFragment

    fun getParams(
        projectId: String,
        subjectId: String,
        scannedCredential: ScannedCredential?
    ) = SelectSubjectParams(projectId, subjectId, scannedCredential)
}
