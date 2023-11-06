package com.simprints.feature.orchestrator

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.face.configuration.FaceConfigurationContract
import com.simprints.matcher.MatchContract
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.clientapi.ClientApiViewModel
import com.simprints.feature.clientapi.extensions.getResultCodeFromExtras
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.feature.logincheck.LoginCheckViewModel
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.databinding.FragmentOrchestratorBinding
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.setup.SetupContract
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.infra.orchestration.data.results.AppResult
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.moduleapi.app.responses.IAppConfirmationResponse
import com.simprints.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppIdentifyResponse
import com.simprints.moduleapi.app.responses.IAppRefusalFormResponse
import com.simprints.moduleapi.app.responses.IAppVerifyResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This fragment serves as the root for the whole orchestrator flow of the app.
 * Execution flow is not linear and has a fair amount of reactivity.
 * Happy path looks approximately like this:
 *   - Intent is received
 *   - Intent data is parsed into [ActionRequest] with clientApiVm.handleIntent()
 *   - ActionRequest is passed to loginCheckVm to verify that the user is signed in
 *   - Login flow might be executed in case the user is not signed in
 *   - After confirming the sing-in state, action is passed to orchestratorVm.handleAction()
 *   - Orchestrator creates a list of steps to be executed and starts executing them one by one
 *   - Step execution is delegated to fragment to perform graph navigation actions for the result
 *   - Once step is completed, orchestratorVm.handleResult() is called to process the result
 *   - After the last step is completed (or if an error/refusal happened), the orchestrator result is returned
 *   - Result is passed to clientApiVm.handle_Response() to be converted into Intent extras
 *   - Orchestrator fragment is finished with the result and appropriately structured extras
 */
@AndroidEntryPoint
internal class OrchestratorFragment : Fragment(R.layout.fragment_orchestrator) {

    private var isActivityRestored = false
    private var requestProcessed = false

    @Inject
    lateinit var alertConfigurationMapper: AlertConfigurationMapper

    @Inject
    lateinit var orchestratorCache: OrchestratorCache

    private val args by navArgs<OrchestratorFragmentArgs>()

    private val loginCheckVm by viewModels<LoginCheckViewModel>()
    private val clientApiVm by viewModels<ClientApiViewModel>()
    private val orchestratorVm by viewModels<OrchestratorViewModel>()

    private val binding by viewBinding(FragmentOrchestratorBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeLoginCheckVm()
        observeClientApiVm()
        observeOrchestratorVm()

        handleResult<AlertResult>(AlertContract.DESTINATION) { alertResult ->
            clientApiVm.handleErrorResponse(
                args.requestAction,
                AppErrorResponse(AlertConfigurationMapper.reasonFromPayload(alertResult.payload))
            )
        }

        handleResult<LoginResult>(LoginContract.DESTINATION) { result ->
            loginCheckVm.handleLoginResult(result)
        }

        // All step results are handled in unified way because some results
        // can be returned from any step (e.g. ExitFormResult)
        handleResult(SetupContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(ConsentContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(SelectSubjectContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(EnrolLastBiometricContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(ExitFormContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(FaceConfigurationContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(MatchContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(FaceCaptureContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(FingerprintCaptureContract.DESTINATION, orchestratorVm::handleResult)
    }

    private fun <T : Parcelable> handleResult(destination: Int, block: (T) -> Unit) {
        findNavController().handleResult(viewLifecycleOwner, R.id.orchestratorRootFragment, destination, block)
    }

    private fun observeLoginCheckVm() {
        loginCheckVm.showAlert.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { error ->
            findNavController().navigate(
                R.id.action_orchestratorFragment_to_alert,
                alertConfigurationMapper.buildAlertConfig(error).toArgs()
            )
        })

        loginCheckVm.showLoginFlow.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { request ->
            findNavController().navigate(
                R.id.action_orchestratorFragment_to_login,
                LoginContract.toArgs(request.projectId, request.userId),
            )
        })

        loginCheckVm.returnLoginNotComplete.observe(viewLifecycleOwner, LiveDataEventObserver {
            clientApiVm.handleErrorResponse(args.requestAction, AppErrorResponse(IAppErrorReason.LOGIN_NOT_COMPLETE))
        })

        loginCheckVm.proceedWithAction.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { action ->
            orchestratorVm.handleAction(action)
        })
    }

    private fun observeClientApiVm() {
        clientApiVm.newSessionCreated.observe(viewLifecycleOwner, LiveDataEventObserver {
            orchestratorCache.clearSteps()
        })
        clientApiVm.showAlert.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { error ->
            findNavController().navigate(
                R.id.action_orchestratorFragment_to_alert,
                alertConfigurationMapper.buildAlertConfig(error).toArgs()
            )
        })
        clientApiVm.returnResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { responseExtras ->
            val resultCode = responseExtras.getResultCodeFromExtras()
            findNavController().finishWithResult(this, AppResult(resultCode, responseExtras))
        })
    }

    private fun observeOrchestratorVm() {
        orchestratorVm.currentStep.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { step ->
            if (step != null) {
                findNavController().navigate(step.navigationActionId, step.payload)
            }
        })
        orchestratorVm.appResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { response ->
            if (response.request == null) {
                clientApiVm.handleErrorResponse(args.requestAction, AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR))
            } else {
                when (response.response) {
                    is IAppEnrolResponse -> clientApiVm.handleEnrolResponse(response.request, response.response)
                    is IAppIdentifyResponse -> clientApiVm.handleIdentifyResponse(response.request, response.response)
                    is IAppConfirmationResponse -> clientApiVm.handleConfirmResponse(response.request, response.response)
                    is IAppVerifyResponse -> clientApiVm.handleVerifyResponse(response.request, response.response)
                    is IAppRefusalFormResponse -> clientApiVm.handleExitFormResponse(response.request, response.response)
                    is IAppErrorResponse -> clientApiVm.handleErrorResponse(args.requestAction, response.response)
                    else -> clientApiVm.handleErrorResponse(args.requestAction, AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR))
                }
            }
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
