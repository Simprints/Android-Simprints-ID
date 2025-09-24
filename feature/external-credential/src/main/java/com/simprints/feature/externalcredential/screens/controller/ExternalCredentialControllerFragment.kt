package com.simprints.feature.externalcredential.screens.controller

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.externalcredential.GraphExternalCredentialInternalDirections
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.navigation.navigationParams
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
internal class ExternalCredentialControllerFragment : Fragment(R.layout.fragment_external_credential_controller) {
    private val params: ExternalCredentialParams by navigationParams()
    private val viewModel: ExternalCredentialViewModel by activityViewModels()

    private val hostFragment: Fragment?
        get() = childFragmentManager.findFragmentById(R.id.external_credential_host_fragment)

    private val internalNavController: NavController?
        get() = hostFragment?.findNavController()

    private val currentlyDisplayedInternalFragment: Fragment?
        get() = hostFragment?.childFragmentManager?.fragments?.first()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init(params)

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
        internalNavController?.setGraph(R.navigation.graph_external_credential_internal)

        initObservers()
        initListeners()
    }

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) {
        }
    }

    private fun initListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when (internalNavController?.currentDestination?.id) {
                R.id.externalCredentialSelectFragment, R.id.externalCredentialSearch -> {
                    // Exit form navigation
                    findNavController().navigateSafely(
                        this@ExternalCredentialControllerFragment,
                        R.id.action_global_refusalFragment,
                    )
                }

                else -> internalNavController?.popBackStack()
            }
        }
    }
}
