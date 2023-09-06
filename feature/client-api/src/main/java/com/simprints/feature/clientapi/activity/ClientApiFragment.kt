package com.simprints.feature.clientapi.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.clientapi.R
import com.simprints.feature.clientapi.databinding.FragmentClientApiBinding
import com.simprints.feature.clientapi.extensions.toMap
import com.simprints.feature.clientapi.logincheck.LoginCheckViewModel
import com.simprints.feature.clientapi.mappers.AlertConfigurationMapper
import com.simprints.feature.clientapi.mappers.response.ActionToIntentMapper
import com.simprints.feature.clientapi.models.ActionRequestIdentifier
import com.simprints.feature.clientapi.models.ClientApiResult
import com.simprints.feature.clientapi.models.ClientApiResultError
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ClientApiFragment : Fragment(R.layout.fragment_client_api) {

    private var isActivityRestored = false
    private var requestProcessed = false

    @Inject
    lateinit var alertConfigurationMapper: AlertConfigurationMapper

    @Inject
    lateinit var resultMapper: ActionToIntentMapper

    private val args by navArgs<ClientApiFragmentArgs>()

    private val loginCheckVm by viewModels<LoginCheckViewModel>()
    private val vm by viewModels<ClientApiViewModel>()

    private val binding by viewBinding(FragmentClientApiBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.clientApiFragment,
            AlertContract.ALERT_DESTINATION_ID,
        ) { alertResult ->
            vm.handleErrorResponse(
                ActionRequestIdentifier.fromIntentAction(args.requestAction),
                ClientApiResultError(AlertConfigurationMapper.reasonFromPayload(alertResult.payload))
            )
        }

        findNavController().handleResult<LoginResult>(
            viewLifecycleOwner,
            R.id.clientApiFragment,
            LoginContract.LOGIN_DESTINATION_ID,
        ) { result -> loginCheckVm.handleLoginResult(result) }

        observeLoginCheckVm()

        vm.returnResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { response ->
            val responseExtras = resultMapper(response)
            val resultCode = responseExtras.getInt(LibSimprintsConstants.RESULT_CODE_OVERRIDE, AppCompatActivity.RESULT_OK)
            responseExtras.remove(LibSimprintsConstants.RESULT_CODE_OVERRIDE)
            findNavController().finishWithResult(this, ClientApiResult(resultCode, responseExtras))
        })
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

        loginCheckVm.returnErrorResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { error ->
            vm.handleErrorResponse(ActionRequestIdentifier.fromIntentAction(args.requestAction), error)
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

    override fun onResume() {
        super.onResume()

        if (!isActivityRestored && !requestProcessed) {
            requestProcessed = true
            lifecycleScope.launch { loginCheckVm.handleIntent(args.requestAction, args.requestParams.toMap()) }
        }
    }

}
