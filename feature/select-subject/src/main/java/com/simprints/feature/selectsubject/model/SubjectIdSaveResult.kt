package com.simprints.feature.selectsubject.model

data class SubjectIdSaveResult(
    val isSubjectIdSaved: Boolean,
    val shouldDisplaySaveCredentialDialog: Boolean
)

data class ExternalCredentialSaveResult(
    val isExternalCredentialIdSaved: Boolean,
)
