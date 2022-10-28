package com.simprints.face.error

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.utils.TimeUtils.getFormattedEstimatedOutage
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.AlertScreenEvent
import com.simprints.face.controllers.core.events.model.FaceAlertType
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.databinding.FragmentErrorBinding
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import javax.inject.Inject

class ErrorFragment : Fragment(R.layout.fragment_error) {
    private val args: ErrorFragmentArgs by navArgs()
    private val mainVm: FaceOrchestratorViewModel by activityViewModels()
    private val binding by viewBinding(FragmentErrorBinding::bind)

    @Inject
    lateinit var faceSessionEventsManager: FaceSessionEventsManager

    @Inject
    lateinit var faceTimeHelper: FaceTimeHelper
    private val startTime = faceTimeHelper.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(args.errorType) {
            binding.errorLayout.setBackgroundColor(
                ContextCompat.getColor(requireContext(), backgroundColor)
            )
            binding.errorTitle.text =
                errorCode?.let { getString(title) + " ($it)" } ?: getString(title)
            binding.errorMessage.text =
                if (estimatedOutage != null && estimatedOutage != 0L) getString(
                    R.string.error_backend_maintenance_with_time_message,
                    getFormattedEstimatedOutage(estimatedOutage!!)
                ) else getString(message)
            binding.errorButton.text = getString(buttonText)
            binding.errorImage.setImageResource(mainDrawable)
        }

        binding.errorButton.setOnClickListener {
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
