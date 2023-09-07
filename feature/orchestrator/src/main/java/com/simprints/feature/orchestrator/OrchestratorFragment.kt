package com.simprints.feature.orchestrator

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.clientapi.ClientApiViewModel
import com.simprints.feature.clientapi.extensions.getResultCodeFromExtras
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.feature.logincheck.LoginCheckViewModel
import com.simprints.feature.orchestrator.databinding.FragmentOrchestratorBinding
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import com.simprints.infra.orchestration.data.results.AppResult
import com.simprints.infra.orchestration.data.results.AppResultError
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.moduleapi.app.responses.IAppErrorReason
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class OrchestratorFragment : Fragment(R.layout.fragment_orchestrator) {

    private var isActivityRestored = false
    private var requestProcessed = false

    @Inject
    lateinit var alertConfigurationMapper: AlertConfigurationMapper

    private val args by navArgs<OrchestratorFragmentArgs>()

    private val loginCheckVm by viewModels<LoginCheckViewModel>()
    private val clientApiVm by viewModels<ClientApiViewModel>()

    private val binding by viewBinding(FragmentOrchestratorBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.orchestratorRootFragment,
            AlertContract.ALERT_DESTINATION_ID,
        ) { alertResult ->
            clientApiVm.handleErrorResponse(
                ActionRequestIdentifier.fromIntentAction(args.requestAction),
                AppResultError(AlertConfigurationMapper.reasonFromPayload(alertResult.payload))
            )
        }

        findNavController().handleResult<LoginResult>(
            viewLifecycleOwner,
            R.id.orchestratorRootFragment,
            LoginContract.LOGIN_DESTINATION_ID,
        ) { result -> loginCheckVm.handleLoginResult(result) }

        observeLoginCheckVm()
        observeClientApiVm()
    }

    private fun observeLoginCheckVm() {
        loginCheckVm.showAlert.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { error ->
            findNavController().navigate(
                R.id.action_clientApiFragment_to_alert,
                alertConfigurationMapper.buildAlertConfig(error).toArgs()
            )
        })

        loginCheckVm.showLoginFlow.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { request ->
            findNavController().navigate(
                R.id.action_clientApiFragment_to_login,
                LoginContract.toArgs(request.projectId, request.userId),
            )
        })

        loginCheckVm.returnLoginNotComplete.observe(viewLifecycleOwner, LiveDataEventObserver {
            clientApiVm.handleErrorResponse(
                ActionRequestIdentifier.fromIntentAction(args.requestAction),
                AppResultError(IAppErrorReason.LOGIN_NOT_COMPLETE)
            )
        })

        loginCheckVm.proceedWithAction.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { action ->
            // TODO add special case for confirmation action
            // TODO replace with user flow builder
            findNavController().navigate(
                R.id.action_clientApiFragment_to_stubFragment,
                StubFragmentArgs(action.toString()).toBundle()
            )
        })
    }

    private fun observeClientApiVm() {
        clientApiVm.showAlert.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { error ->
            findNavController().navigate(
                R.id.action_clientApiFragment_to_alert,
                alertConfigurationMapper.buildAlertConfig(error).toArgs()
            )
        })

        clientApiVm.returnResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { responseExtras ->
            val resultCode = responseExtras.getResultCodeFromExtras()
            findNavController().finishWithResult(this, AppResult(resultCode, responseExtras))
        })
    }

    override fun onResume() {
        super.onResume()

        if (!isActivityRestored && !requestProcessed) {
            if (loginCheckVm.isDeviceSafe()) {
                requestProcessed = true
                lifecycleScope.launch {
                    val actionRequest = clientApiVm.handleIntent(args.requestAction, args.requestParams)
                    if (actionRequest != null) {
                        loginCheckVm.validateSignInAndProceed(actionRequest)
                    }
                }
            }
        }
    }

}
