package com.simprints.feature.fetchsubject.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectParams
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.fetchsubject.R
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.navigation.navigationParams
import com.simprints.infra.uibase.view.applySystemBarInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class FetchSubjectFragment : Fragment(R.layout.fragment_subject_fetch) {
    private val viewModel: FetchSubjectViewModel by viewModels()
    private val params: FetchSubjectParams by navigationParams()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("FetchSubjectFragment started", tag = ORCHESTRATION)

        with(findNavController()) {
            handleResult(viewLifecycleOwner, R.id.fetchSubjectFragment, AlertContract.DESTINATION, ::handleAlertResult)
            handleResult(viewLifecycleOwner, R.id.fetchSubjectFragment, ExitFormContract.DESTINATION, ::handleExitFormResult)
        }

        viewModel.subjectState.observe(viewLifecycleOwner) { state ->
            state?.getContentIfNotHandled()?.let(::handleFetchState)
        }

        viewModel.onViewCreated(params.projectId, params.subjectId)
    }

    private fun handleAlertResult(alertResult: AlertResult) {
        when (alertResult.buttonKey) {
            FetchSubjectAlerts.ACTION_CLOSE -> {
                findNavController().finishWithResult(this, alertResult)
            }

            FetchSubjectAlerts.ACTION_RETRY -> tryFetchSubject()
            AlertContract.ALERT_BUTTON_PRESSED_BACK -> viewModel.startExitForm()
        }
    }

    private fun tryFetchSubject() {
        viewModel.fetchSubject(params.projectId, params.subjectId)
    }

    private fun handleFetchState(state: FetchSubjectState) = when (state) {
        FetchSubjectState.FoundLocal,
        FetchSubjectState.FoundRemote,
        -> finishWithResult(true)

        FetchSubjectState.NotFound -> openAlert(FetchSubjectAlerts.subjectNotFoundOnline().toArgs())
        FetchSubjectState.ConnectionError -> openAlert(FetchSubjectAlerts.subjectNotFoundOffline().toArgs())
        is FetchSubjectState.ShowExitForm -> openExitForm()
    }

    private fun openAlert(alertArgs: Bundle) {
        findNavController().navigateSafely(this, R.id.action_fetchSubjectFragment_to_errorFragment, alertArgs)
    }

    private fun openExitForm() {
        findNavController().navigateSafely(this, R.id.action_fetchSubjectFragment_to_exitFormFragment)
    }

    private fun finishWithResult(
        found: Boolean,
        wasOnline: Boolean = false,
    ) {
        findNavController().finishWithResult(this, FetchSubjectResult(found, wasOnline))
    }

    private fun handleExitFormResult(exiFormResult: ExitFormResult) {
        findNavController().finishWithResult(this, exiFormResult)
    }
}
