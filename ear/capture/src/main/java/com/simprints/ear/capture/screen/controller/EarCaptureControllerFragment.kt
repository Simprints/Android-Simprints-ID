package com.simprints.ear.capture.screen.controller

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
import com.simprints.ear.capture.GraphEarCaptureInternalDirections
import com.simprints.ear.capture.R
import com.simprints.ear.capture.screen.EarCaptureViewModel
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
internal class EarCaptureControllerFragment : Fragment(R.layout.fragment_ear_capture) {
    private val args: EarCaptureControllerFragmentArgs by navArgs()

    private val viewModel: EarCaptureViewModel by activityViewModels()

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
        Simber.i("EarCaptureControllerFragment started", tag = ORCHESTRATION)

        findNavController().handleResult<ExitFormResult>(
            this,
            R.id.earCaptureControllerFragment,
            ExitFormContract.DESTINATION,
        ) {
            val option = it.submittedOption()
            if (option != null) {
                findNavController().finishWithResult(this, it)
            } else {
                internalNavController?.navigateSafely(
                    currentlyDisplayedInternalFragment,
                    GraphEarCaptureInternalDirections.actionGlobalEarPreviewFeedback(),
                )
            }
        }

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.earPreviewFeedbackFragment,
            AlertContract.DESTINATION,
        ) { result ->
            findNavController().finishWithResult(this, result)
        }

        viewModel.setupCapture(args.samplesToCapture)
        viewModel.initialize(requireActivity())
        viewModel.recaptureEvent.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                internalNavController?.navigateSafely(
                    currentlyDisplayedInternalFragment,
                    GraphEarCaptureInternalDirections.actionGlobalEarPreviewFeedback(),
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
                R.id.earPreviewFeedbackFragment -> viewModel.handleBackButton()
                else -> findNavController().popBackStack()
            }
        }
        internalNavController?.setGraph(R.navigation.graph_ear_capture_internal)
    }
}
