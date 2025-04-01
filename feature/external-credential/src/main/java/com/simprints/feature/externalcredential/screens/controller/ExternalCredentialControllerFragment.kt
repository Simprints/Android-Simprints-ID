package com.simprints.feature.externalcredential.screens.controller

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.externalcredential.GraphExternalCredentialInternalDirections
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.feature.externalcredential.R
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely

@AndroidEntryPoint
class ExternalCredentialControllerFragment : Fragment(R.layout.fragment_external_credential_controller){
    private val args: ExternalCredentialControllerFragmentArgs by navArgs()

    private val viewModel: ExternalCredentialViewModel by activityViewModels()

    private val hostFragment: Fragment?
        get() = childFragmentManager.findFragmentById(R.id.external_credential_host_fragment)

    private val internalNavController: NavController?
        get() = hostFragment?.findNavController()

    private val currentlyDisplayedInternalFragment: Fragment?
        get() = hostFragment?.childFragmentManager?.fragments?.first()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<ExitFormResult>(
            this,
            R.id.externalCredentialControllerFragment,
            ExitFormContract.DESTINATION,
        ) {
            val option = it.submittedOption()
            if (option != null) {
                findNavController().finishWithResult(this, it)
            } else {
                internalNavController?.navigateSafely(
                    currentlyDisplayedInternalFragment,
                    GraphExternalCredentialInternalDirections.actionGlobalExternalCredentialSelect()
                )
            }
        }

        viewModel.setConfig(args.externalCredentialParams)

        internalNavController?.setGraph(R.navigation.graph_external_credential_internal)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.externalCredentialSaveResponse.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                findNavController().finishWithResult(this, it)
            },
        )

    }

}
