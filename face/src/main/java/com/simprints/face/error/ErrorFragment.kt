package com.simprints.face.error

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simprints.face.R
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.AlertScreenEvent
import com.simprints.face.controllers.core.events.model.FaceAlertType
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import kotlinx.android.synthetic.main.fragment_error.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ErrorFragment : Fragment(R.layout.fragment_error) {
    private val args: ErrorFragmentArgs by navArgs()
    private val mainVm: FaceOrchestratorViewModel by sharedViewModel()
    private val faceSessionEventsManager: FaceSessionEventsManager by inject()
    private val faceTimeHelper: FaceTimeHelper by inject()
    private val startTime = faceTimeHelper.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(args.errorType) {
            errorLayout.setBackgroundColor(
                ContextCompat.getColor(requireContext(), backgroundColor)
            )
            errorTitle.text = getString(title)
            errorMessage.text = getString(message)
            errorButton.text = getString(buttonText)
            errorImage.setImageResource(mainDrawable)
        }

        errorButton.setOnClickListener {
            sendAlertEvent(args.errorType)
            mainVm.finishWithError(args.errorType)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            sendAlertEvent(args.errorType)
            mainVm.finishWithError(args.errorType)
        }
    }

    private fun sendAlertEvent(errorType: ErrorType) {
        faceSessionEventsManager.addEvent(
            AlertScreenEvent(startTime, FaceAlertType.fromErrorType(errorType))
        )
    }
}
