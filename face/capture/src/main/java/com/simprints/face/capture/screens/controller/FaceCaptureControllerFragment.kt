package com.simprints.face.capture.screens.controller

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.face.capture.GraphFaceCaptureInternalDirections
import com.simprints.face.capture.R
import com.simprints.face.capture.screens.FaceCaptureViewModel
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class FaceCaptureControllerFragment : Fragment(R.layout.fragment_face_capture) {
    private val args: FaceCaptureControllerFragmentArgs by navArgs()

    private val viewModel: FaceCaptureViewModel by activityViewModels()

    private val hostFragment: Fragment?
        get() = childFragmentManager
            .findFragmentById(R.id.orchestrator_host_fragment)

    private val internalNavController: NavController?
        get() = hostFragment?.findNavController()

    private val currentlyDisplayedInternalFragment: Fragment?
        get() = hostFragment?.childFragmentManager?.fragments?.first()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("FaceCaptureControllerFragment started", tag = ORCHESTRATION)

        findNavController().handleResult<ExitFormResult>(
            this,
            R.id.faceCaptureControllerFragment,
            ExitFormContract.DESTINATION,
        ) {
            val option = it.submittedOption()
            if (option != null) {
                findNavController().finishWithResult(this, it)
            } else {
                internalNavController?.navigateSafely(
                    currentlyDisplayedInternalFragment,
                    GraphFaceCaptureInternalDirections.actionGlobalFaceLiveFeedback(),
                )
            }
        }

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.faceCaptureControllerFragment,
            AlertContract.DESTINATION,
        ) { result ->
            findNavController().finishWithResult(this, result)
        }

        viewModel.setupCapture(args.samplesToCapture)
        initFaceBioSdk()
        viewModel.recaptureEvent.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                internalNavController?.navigateSafely(
                    currentlyDisplayedInternalFragment,
                    GraphFaceCaptureInternalDirections.actionGlobalFaceLiveFeedback(),
                )
            },
        )

        viewModel.exitFormEvent.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                findNavController().navigateSafely(
                    this,
                    R.id.action_global_refusalFragment,
                )
            },
        )

        viewModel.unexpectedErrorEvent.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                findNavController().navigateUp()
            },
        )

        viewModel.finishFlowEvent.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                findNavController().finishWithResult(this, it)
            },
        )

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when (internalNavController?.currentDestination?.id) {
                R.id.facePreparationFragment,
                R.id.faceLiveFeedbackFragment,
                -> viewModel.handleBackButton()

                else -> findNavController().popBackStack()
            }
        }

        viewModel.setupAutoCapture()
        viewModel.isAutoCaptureEnabled.observe(viewLifecycleOwner) { isAutoCaptureEnabled ->
            internalNavController?.setGraph(
                if (isAutoCaptureEnabled) {
                    R.navigation.graph_face_capture_auto_internal
                } else {
                    R.navigation.graph_face_capture_internal
                },
            )
        }
    }

    private fun initFaceBioSdk() {
        viewModel.invalidLicense.observe(viewLifecycleOwner) {
            findNavController().navigateSafely(
                this,
                R.id.action_global_errorFragment,
                InvalidFaceLicenseAlert.toAlertArgs(),
            )
        }
        viewModel.initFaceBioSdk(requireActivity())
    }
}
