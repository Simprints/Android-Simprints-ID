package com.simprints.feature.enrollast.screen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.LinearProgressIndicator
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
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType.NO_MATCH_RESULTS
import com.simprints.feature.enrollast.screen.EnrolLastState.ErrorType.EXTERNAL_CREDENTIAL_SAVE_ERROR
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class EnrolLastBiometricFragment : Fragment(R.layout.fragment_enrol_last) {
    private val viewModel: EnrolLastBiometricViewModel by viewModels()
    private val args: EnrolLastBiometricFragmentArgs by navArgs()
    private var dialog: Dialog? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("EnrolLastBiometricFragment started", tag = ORCHESTRATION)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.enrolLastBiometricFragment,
            AlertContract.DESTINATION,
        ) { finishWithSubjectId(null) }

        viewModel.enrolLastStateLiveData.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            processEnrolResult(it)
        })
        viewModel.finish.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            displayExternalCredentialSaveAnimation {
                finishSavingAndNavigateToCalloutApp(it.newGuid)
            }
        })
        viewModel.onViewCreated(args.params)
    }

    private fun displayExternalCredentialSaveAnimation(onComplete: () -> Unit) {
        val saveCredentialProgressLayout = requireView().findViewById<View>(R.id.saveCredentialProgressLayout)
        saveCredentialProgressLayout.isVisible = true

        val progressBar = requireView().findViewById<LinearProgressIndicator>(R.id.saveProgressBar)
        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
        animator.duration = 1500

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete()
            }
        })
        animator.start()
    }

    private fun processExternalCredentialSave(newGuid: String, externalCred: String, externalCredImagePath: String?) {
        displayExternalCredentialPreviewDialog(
            externalCred = externalCred,
            externalCredImagePath = externalCredImagePath,
            onConfirm = {
                dialog?.dismiss()
                dialog = null
                viewModel.saveExternalCredential(externalCred = externalCred, subjectId = newGuid)
            },
            onSkip = {
                dialog?.dismiss()
                dialog = null
                finishSavingAndNavigateToCalloutApp(newGuid)
            },
        )
    }

    private fun displayExternalCredentialPreviewDialog(
        externalCred: String,
        externalCredImagePath: String?,
        onConfirm: () -> Unit,
        onSkip: () -> Unit
    ) {
        dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_external_credential_preview, null)
        val confirmButton = view.findViewById<Button>(R.id.buttonConfirm)
        val skipButton = view.findViewById<Button>(R.id.buttonSkip)
        val credentialText = view.findViewById<TextView>(R.id.externalCredentialText)
        val imageCardPreview = view.findViewById<ImageView>(R.id.imageCardPreview)
        val confirmCheckbox = view.findViewById<CheckBox>(R.id.confirmCredentialCheckbox)

        credentialText.text = externalCred
        getImage(externalCredImagePath).let { image ->
            imageCardPreview.isVisible = image != null
            if (image != null) {
                imageCardPreview.setImageBitmap(image)
            }
        }

        confirmCheckbox.setOnCheckedChangeListener { _, isChecked ->
            confirmButton.isEnabled = isChecked
        }

        confirmButton.setOnClickListener { onConfirm() }
        skipButton.setOnClickListener { onSkip() }

        dialog?.setContentView(view)
        dialog?.setCancelable(false)
        dialog?.show()
        (dialog as? BottomSheetDialog)?.apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }

    }

    override fun onDestroyView() {
        dialog?.dismiss()
        dialog = null
        super.onDestroyView()
    }

    private fun getImage(path: String?): Bitmap? =
        try {
            path?.run(BitmapFactory::decodeFile)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to get [$path] preview image: ${e.message}", Toast.LENGTH_LONG).show()
            Simber.e("Unable to get [$path] image", e)
            null
        }

    private fun processEnrolResult(result: EnrolLastState) = when (result) {
        is EnrolLastState.Failed -> showError(result.errorType, result.modalities)
        is EnrolLastState.Success -> {
            val externalCred = args.params.externalCredentialId
            if (externalCred == null) {
                finishSavingAndNavigateToCalloutApp(result.newGuid)
            } else {
                val externalCredImagePath = args.params.externalCredentialImagePath
                processExternalCredentialSave(
                    newGuid = result.newGuid,
                    externalCred = externalCred,
                    externalCredImagePath = externalCredImagePath
                )
            }
        }
    }

    private fun finishSavingAndNavigateToCalloutApp(newGuid: String) {
        Toast.makeText(requireContext(), getString(IDR.string.enrol_last_biometrics_success), Toast.LENGTH_LONG).show()
        finishWithSubjectId(newGuid)
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
        NO_MATCH_RESULTS -> IDR.string.enrol_last_biometrics_alert_message
        DUPLICATE_ENROLMENTS -> IDR.string.enrol_last_biometrics_alert_message_duplicate_records
        GENERAL_ERROR -> IDR.string.enrol_last_biometrics_alert_message
        EXTERNAL_CREDENTIAL_SAVE_ERROR -> IDR.string.enrol_last_biometrics_alert_message_external_credential
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
