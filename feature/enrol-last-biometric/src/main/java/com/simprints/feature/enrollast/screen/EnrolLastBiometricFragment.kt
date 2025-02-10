package com.simprints.feature.enrollast.screen

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.R
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType.GENERAL_ERROR
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class EnrolLastBiometricFragment : Fragment(R.layout.fragment_enrol_last) {
    private val viewModel: EnrolLastBiometricViewModel by viewModels()
    private val args: EnrolLastBiometricFragmentArgs by navArgs()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("EnrolLastBiometricFragment started", tag = ORCHESTRATION)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.enrolLastBiometricFragment,
            AlertContract.DESTINATION,
        ) { finishWithSubjectId(null) }

        viewModel.finish.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { finishWithResult(it) })
        viewModel.onViewCreated(args.params)
    }

    private fun finishWithResult(result: EnrolLastState) = when (result) {
        is EnrolLastState.Failed -> showError(result.errorType, result.modalities)
        is EnrolLastState.Success -> {
            Toast.makeText(requireContext(), getString(IDR.string.enrol_last_biometrics_success), Toast.LENGTH_LONG).show()
            finishWithSubjectId(result.newGuid)
        }
    }

    private fun showError(
        errorType: ErrorType,
        modalities: List<Modality>,
    ) {
        findNavController().navigateSafely(
            this,
            R.id.action_enrolLastBiometricFragment_to_errorFragment,
            createAlertConfiguration(errorType, modalities).toArgs(),
        )
    }

    private fun createAlertConfiguration(
        errorType: ErrorType,
        modalities: List<Modality>,
    ) = alertConfiguration {
        color = AlertColor.Gray
        titleRes = IDR.string.enrol_last_biometrics_alert_title
        message = getString(getAlertMessage(errorType), getModalityName(modalities))
        leftButton = AlertButtonConfig.Close
        appErrorReason = AppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED
        eventType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED
    }

    private fun getAlertMessage(errorType: ErrorType) = when (errorType) {
        DUPLICATE_ENROLMENTS -> IDR.string.enrol_last_biometrics_alert_message_duplicate_records
        GENERAL_ERROR -> IDR.string.enrol_last_biometrics_alert_message
    }

    private fun getModalityName(modalities: List<Modality>) = modalities
        .let {
            when {
                it.size >= 2 -> IDR.string.enrol_last_biometrics_alert_message_all_param
                it.contains(Modality.FACE) -> IDR.string.enrol_last_biometrics_alert_message_face_param
                it.contains(Modality.FINGERPRINT) -> IDR.string.enrol_last_biometrics_alert_message_fingerprint_param
                else -> IDR.string.enrol_last_biometrics_alert_message_all_param
            }
        }.let { getString(it) }

    private fun finishWithSubjectId(newSubjectId: String?) {
        findNavController().finishWithResult(this, EnrolLastBiometricResult(newSubjectId))
    }
}
