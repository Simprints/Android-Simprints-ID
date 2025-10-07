package com.simprints.feature.selectsubject.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal sealed class SelectSubjectState {
    data object SavingSubjectId : SelectSubjectState()

    data object SavingExternalCredential : SelectSubjectState()

    data class CredentialDialogDisplayed(
        val scannedCredential: ScannedCredential,
        val displayedCredential: TokenizableString.Raw,
    ) : SelectSubjectState()

    companion object {
        val EMPTY = SelectSubjectState.SavingSubjectId
    }
}
