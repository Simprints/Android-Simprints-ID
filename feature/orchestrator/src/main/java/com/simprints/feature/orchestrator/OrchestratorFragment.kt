package com.simprints.feature.orchestrator

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.chatbot.context.ChatContextProvider
import com.simprints.feature.clientapi.ClientApiViewModel
import com.simprints.feature.clientapi.extensions.getResultCodeFromExtras
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.enrollast.EnrolLastBiometricContract
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.externalcredential.ExternalCredentialContract
import com.simprints.feature.fetchsubject.FetchSubjectContract
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.feature.logincheck.LoginCheckError
import com.simprints.feature.logincheck.LoginCheckViewModel
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupContract
import com.simprints.feature.selectsubject.SelectSubjectContract
import com.simprints.feature.setup.SetupContract
import com.simprints.feature.validatepool.ValidateSubjectPoolContract
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.responses.AppConfirmationResponse
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import com.simprints.infra.orchestration.data.results.AppResult
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.navigation.toBundle
import com.simprints.matcher.MatchContract
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
    @Inject
    lateinit var alertConfigurationMapper: AlertConfigurationMapper

    @Inject
    lateinit var orchestratorCache: OrchestratorCache

    @Inject
    lateinit var chatContextProvider: ChatContextProvider

    private val args by navArgs<OrchestratorFragmentArgs>()

    private val loginCheckVm by viewModels<LoginCheckViewModel>()
    private val clientApiVm by viewModels<ClientApiViewModel>()
    private val orchestratorVm by viewModels<OrchestratorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            orchestratorVm.isRequestProcessed = savedInstanceState.getBoolean(KEY_REQUEST_PROCESSED)
            savedInstanceState
                .getString(KEY_ACTION_REQUEST)
                ?.run(orchestratorVm::setActionRequestFromJson)
            Simber.i("Attempting to restore OrchestratorFragment state", tag = ORCHESTRATION)
            orchestratorVm.restoreStepsIfNeeded()
            orchestratorVm.restoreModalitiesIfNeeded()
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        observeLoginCheckVm()
        observeClientApiVm()
        observeOrchestratorVm()

        handleResult<AlertResult>(AlertContract.DESTINATION) { alertResult ->
            Simber.i("Alert result: $alertResult", tag = ORCHESTRATION)
            chatContextProvider.clearActiveAlert()
            orchestratorVm.handleErrorResponse(
                AppErrorResponse(alertResult.appErrorReason ?: AppErrorReason.UNEXPECTED_ERROR),
            )
        }

        handleResult<LoginResult>(LoginContract.DESTINATION) { result ->
            Simber.i("Sign-in result: $result", tag = ORCHESTRATION)
            loginCheckVm.handleLoginResult(result)
        }

        // All step results are handled in unified way because some results
        // can be returned from any step (e.g. ExitFormResult)
        handleResult(SetupContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(ConsentContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(SelectSubjectContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(EnrolLastBiometricContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(ExitFormContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(MatchContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(FaceCaptureContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(FingerprintCaptureContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(FetchSubjectContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(ValidateSubjectPoolContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(SelectSubjectAgeGroupContract.DESTINATION, orchestratorVm::handleResult)
        handleResult(ExternalCredentialContract.DESTINATION, orchestratorVm::handleResult)
    }

    private fun <T> handleResult(
        destination: Int,
        block: (T) -> Unit,
    ) {
        findNavController().handleResult(
            lifecycleOwner = viewLifecycleOwner,
            currentDestinationId = R.id.orchestratorRootFragment,
            targetDestinationId = destination,
            handler = block,
        )
    }

    private fun observeLoginCheckVm() {
        loginCheckVm.showAlert.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { error ->
                chatContextProvider.updateActiveAlert(describeLoginCheckAlert(error))
                findNavController().navigateSafely(
                    currentFragment = this,
                    actionId = R.id.action_orchestratorFragment_to_alert,
                    args = alertConfigurationMapper.buildAlertConfig(error).toArgs(),
                )
            },
        )

        loginCheckVm.showLoginFlow.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { request ->
                findNavController().navigateSafely(
                    currentFragment = this,
                    actionId = R.id.action_orchestratorFragment_to_login,
                    args = LoginContract.getParams(request.projectId, request.userId).toBundle(),
                )
            },
        )

        loginCheckVm.returnLoginNotComplete.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                orchestratorVm.handleErrorResponse(
                    AppErrorResponse(AppErrorReason.LOGIN_NOT_COMPLETE),
                )
            },
        )

        loginCheckVm.proceedWithAction.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { action ->
                Simber.i("Login check complete, starting orchestrator", tag = ORCHESTRATION)
                orchestratorVm.handleAction(action)
            },
        )
    }

    private fun observeClientApiVm() {
        clientApiVm.newSessionCreated.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                Simber.i("New session created, caches cleared", tag = ORCHESTRATION)
                orchestratorCache.clearCache()
            },
        )
        clientApiVm.showAlert.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { error ->
                chatContextProvider.updateActiveAlert(describeClientApiAlert(error))
                findNavController().navigateSafely(
                    currentFragment = this,
                    actionId = R.id.action_orchestratorFragment_to_alert,
                    args = alertConfigurationMapper.buildAlertConfig(error).toArgs(),
                )
            },
        )
        clientApiVm.returnResponse.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { responseExtras ->
                val resultCode = responseExtras.getResultCodeFromExtras()
                findNavController().finishWithResult(this, AppResult(resultCode, responseExtras))
            },
        )
    }

    private fun observeOrchestratorVm() {
        orchestratorVm.currentStep.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { step ->
                if (step != null) {
                    Simber.i("Executing step: ${step.id}", tag = ORCHESTRATION)
                    findNavController().navigateSafely(
                        currentFragment = this,
                        actionId = step.navigationActionId,
                        args = step.params.toBundle(),
                    )
                }
            },
        )
        orchestratorVm.appResponse.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { response ->
                Simber.i("Responding to ${response.request?.actionIdentifier?.actionName}", tag = ORCHESTRATION)
                Simber.i("Responding with ${response.response.javaClass.simpleName}", tag = ORCHESTRATION)

                if (response.request == null) {
                    clientApiVm.handleErrorResponse(
                        args.requestAction,
                        AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR),
                    )
                } else {
                    when (response.response) {
                        is AppEnrolResponse -> {
                            clientApiVm.handleEnrolResponse(response.request, response.response)
                        }

                        is AppIdentifyResponse -> {
                            clientApiVm.handleIdentifyResponse(response.request, response.response)
                        }

                        is AppConfirmationResponse -> {
                            clientApiVm.handleConfirmResponse(response.request, response.response)
                        }

                        is AppVerifyResponse -> {
                            clientApiVm.handleVerifyResponse(response.request, response.response)
                        }

                        is AppRefusalResponse -> {
                            clientApiVm.handleExitFormResponse(response.request, response.response)
                        }

                        is AppErrorResponse -> {
                            clientApiVm.handleErrorResponse(args.requestAction, response.response)
                        }
                    }
                }
            },
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Simber.i("Saving OrchestratorFragment state", tag = ORCHESTRATION)
        outState.putBoolean(KEY_REQUEST_PROCESSED, orchestratorVm.isRequestProcessed)
        // [MS-405] Saving the action request in the bundle, since ViewModels don't survive the
        // process death. ActionRequest is important in mapping the correct SID response, hence it
        // is important for it to be able to survive both configuration changes and process death.
        outState.putString(KEY_ACTION_REQUEST, orchestratorVm.getActionRequestJson())
    }

    override fun onResume() {
        super.onResume()

        if (!orchestratorVm.isRequestProcessed) {
            Simber.i("Start processing action request", tag = ORCHESTRATION)
            if (loginCheckVm.isDeviceSafe()) {
                lifecycleScope.launch {
                    val actionRequest =
                        clientApiVm.handleIntent(args.requestAction, args.requestParams)
                    if (actionRequest != null) {
                        Simber.i("Action request parsed successfully", tag = ORCHESTRATION)
                        loginCheckVm.validateSignInAndProceed(actionRequest)
                    }
                    orchestratorVm.isRequestProcessed = true
                }
            }
        }
    }

    private fun describeLoginCheckAlert(error: LoginCheckError): String = when (error) {
        LoginCheckError.DIFFERENT_PROJECT_ID ->
            "Configuration Error: The project ID in the request doesn't match the project the user is signed into. " +
                "The user needs to sign in with the correct project or the calling app needs to send the correct project ID."
        LoginCheckError.PROJECT_PAUSED ->
            "Project Paused: This project has been paused by the project administrator. " +
                "No workflows can be executed until the project is resumed."
        LoginCheckError.PROJECT_ENDING ->
            "Project Ending: This project is ending and will be decommissioned. " +
                "The user should contact their project administrator."
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES ->
            "Missing Google Play Services: The device does not have Google Play Services installed. " +
                "Google Play Services is required for the app to function."
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED ->
            "Outdated Google Play Services: The Google Play Services on this device is outdated. " +
                "The user needs to update Google Play Services."
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP ->
            "Missing or Outdated Google Play Store: The Google Play Store app is missing or outdated. " +
                "The user needs to install or update the Google Play Store."
        LoginCheckError.INTEGRITY_SERVICE_ERROR ->
            "Integrity Service Error: The device integrity check failed. " +
                "This may indicate a problem with Google Play Services or the device's security state."
        LoginCheckError.UNEXPECTED_LOGIN_ERROR ->
            "Unexpected Login Error: An unexpected error occurred during the login process. " +
                "The user should try again or check their internet connection."
        LoginCheckError.ROOTED_DEVICE ->
            "Rooted Device Detected: This device has been rooted, which is a security risk. " +
                "The app cannot operate on rooted devices for data protection reasons."
    }

    private fun describeClientApiAlert(error: ClientApiError): String = when (error) {
        ClientApiError.INVALID_STATE_FOR_INTENT_ACTION ->
            "Invalid Intent Action: The calling app sent an action that is not valid in the current state."
        ClientApiError.INVALID_METADATA ->
            "Invalid Metadata: The metadata provided by the calling app is malformed or invalid."
        ClientApiError.INVALID_MODULE_ID ->
            "Invalid Module ID: The module ID provided by the calling app is invalid or not recognized."
        ClientApiError.INVALID_PROJECT_ID ->
            "Invalid Project ID: The project ID provided by the calling app is invalid."
        ClientApiError.INVALID_SELECTED_ID ->
            "Invalid Selected ID: The selected subject ID for confirm identity is invalid."
        ClientApiError.INVALID_SESSION_ID ->
            "Invalid Session ID: The session ID for a follow-up action is invalid or expired."
        ClientApiError.INVALID_USER_ID ->
            "Invalid User ID: The user ID provided by the calling app is invalid."
        ClientApiError.INVALID_VERIFY_ID ->
            "Invalid Verify GUID: The GUID provided for verification is invalid."
    }

    companion object {
        private const val KEY_REQUEST_PROCESSED = "requestProcessed"
        private const val KEY_ACTION_REQUEST = "actionRequest"
    }
}
