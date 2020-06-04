package com.simprints.face.error

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simprints.face.R
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelper
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import kotlinx.android.synthetic.main.fragment_error.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ErrorFragment : Fragment(R.layout.fragment_error) {
    private val args: ErrorFragmentArgs by navArgs()
    private val mainVm: FaceOrchestratorViewModel by sharedViewModel()
    private val androidResourcesHelper: FaceAndroidResourcesHelper by inject()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(args.errorType) {
            errorLayout.setBackgroundColor(
                ContextCompat.getColor(requireContext(), backgroundColor)
            )
            errorTitle.text = androidResourcesHelper.getString(title)
            errorMessage.text = androidResourcesHelper.getString(message)
            errorButton.text = androidResourcesHelper.getString(buttonText)
            errorImage.setImageResource(mainDrawable)
        }

        errorButton.setOnClickListener {
            mainVm.finishWithError(args.errorType)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainVm.finishWithError(args.errorType)
        }
    }
}
