package com.simprints.feature.fetchsubject.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.feature.fetchsubject.R
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.exitFormConfiguration
import com.simprints.feature.exitform.scannerOptions
import com.simprints.feature.exitform.toArgs
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class FetchSubjectFragment : Fragment(R.layout.fragment_subject_fetch) {

    private val viewModel: FetchSubjectViewModel by viewModels()
    private val args: FetchSubjectFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(findNavController()) {
            handleResult(viewLifecycleOwner, R.id.fetchSubjectFragment, AlertContract.ALERT_DESTINATION_ID, ::handleAlertResult)
            handleResult(viewLifecycleOwner, R.id.fetchSubjectFragment, ExitFormContract.DESTINATION_ID, ::handleExitFormResult)
        }

        viewModel.subjectState.observe(viewLifecycleOwner) { state ->
            state?.getContentIfNotHandled()?.let(::handleFetchState)
        }

        viewModel.onViewCreated(args.projectId, args.subjectId)
    }

    private fun handleAlertResult(alertResult: AlertResult) {
        when (alertResult.buttonKey) {
            FetchSubjectAlerts.ACTION_CLOSE -> finishWithResult(false, FetchSubjectAlerts.wasOnline(alertResult.payload))
            FetchSubjectAlerts.ACTION_RETRY -> tryFetchSubject()
            AlertContract.ALERT_BUTTON_PRESSED_BACK -> viewModel.startExitForm()
        }
    }

    private fun tryFetchSubject() {
        viewModel.fetchSubject(args.projectId, args.subjectId)
    }

    private fun handleFetchState(state: FetchSubjectState) = when (state) {
        FetchSubjectState.FoundLocal,
        FetchSubjectState.FoundRemote -> finishWithResult(true)

        FetchSubjectState.NotFound -> openAlert(FetchSubjectAlerts.subjectNotFoundOnline().toArgs())
        FetchSubjectState.ConnectionError -> openAlert(FetchSubjectAlerts.subjectNotFoundOffline().toArgs())
        is FetchSubjectState.ShowExitForm -> openExitForm(getExitFormFromModalities(state.modalities))
    }

    private fun getExitFormFromModalities(modalities: List<GeneralConfiguration.Modality>): Bundle = when {
        modalities.size == 1 && modalities.first() == GeneralConfiguration.Modality.FACE -> exitFormConfiguration {
            titleRes = IDR.string.why_did_you_skip_face_capture
            backButtonRes = IDR.string.exit_form_capture_face
        }

        modalities.size == 1 && modalities.first() == GeneralConfiguration.Modality.FINGERPRINT -> exitFormConfiguration {
            titleRes = IDR.string.why_did_you_skip_fingerprinting
            backButtonRes = IDR.string.button_scan_prints
            visibleOptions = scannerOptions()
        }

        else -> exitFormConfiguration {
            titleRes = IDR.string.why_did_you_skip_biometrics
            backButtonRes = IDR.string.button_scan_prints
            visibleOptions = scannerOptions()
        }
    }.toArgs()

    private fun openAlert(alertArgs: Bundle) {
        findNavController().navigate(R.id.action_fetchSubjectFragment_to_errorFragment, alertArgs)
    }

    private fun openExitForm(exitFormArgs: Bundle) {
        findNavController().navigate(R.id.action_fetchSubjectFragment_to_exitFormFragment, exitFormArgs)
    }

    private fun finishWithResult(found: Boolean, wasOnline: Boolean = false) {
        findNavController().finishWithResult(this, FetchSubjectResult(found, wasOnline))
    }

    private fun handleExitFormResult(exiFormResult: ExitFormResult) {
        findNavController().finishWithResult(this, exiFormResult)
    }
}
