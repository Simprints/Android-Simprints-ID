package com.simprints.document.capture.screens.controller

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.document.capture.GraphDocumentCaptureInternalDirections
import com.simprints.document.capture.R
import com.simprints.document.capture.screens.DocumentCaptureViewModel
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
internal class DocumentCaptureControllerFragment : Fragment(R.layout.fragment_document_capture) {
    private val viewModel: DocumentCaptureViewModel by activityViewModels()

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
        Simber.i("DocumentCaptureControllerFragment started", tag = ORCHESTRATION)

        findNavController().handleResult<ExitFormResult>(
            this,
            R.id.documentCaptureControllerFragment,
            ExitFormContract.DESTINATION,
        ) {
            val option = it.submittedOption()
            if (option != null) {
                findNavController().finishWithResult(this, it)
            } else {
                internalNavController?.navigateSafely(
                    currentlyDisplayedInternalFragment,
                    GraphDocumentCaptureInternalDirections.actionGlobalDocumentLiveFeedback(),
                )
            }
        }

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.documentCaptureControllerFragment,
            AlertContract.DESTINATION,
        ) { result ->
            findNavController().finishWithResult(this, result)
        }

        initFaceBioSdk()
        viewModel.recaptureEvent.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                internalNavController?.navigateSafely(
                    currentlyDisplayedInternalFragment,
                    GraphDocumentCaptureInternalDirections.actionGlobalDocumentLiveFeedback(),
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
                R.id.documentPreparationFragment,
                R.id.documentLiveFeedbackFragment,
                -> viewModel.handleBackButton()

                else -> findNavController().popBackStack()
            }
        }

        internalNavController?.setGraph(R.navigation.graph_document_capture_internal)
    }

    private fun initFaceBioSdk() {
        viewModel.initDocumentSdk(requireActivity())
    }
}
