package com.simprints.feature.clientapi.activity

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.alert.toArgs
import com.simprints.feature.clientapi.R
import com.simprints.feature.clientapi.databinding.FragmentClientApiBinding
import com.simprints.feature.clientapi.extensions.toMap
import com.simprints.feature.clientapi.mappers.AlertConfigurationMapper
import com.simprints.feature.clientapi.mappers.response.ActionToIntentMapper
import com.simprints.feature.clientapi.models.LibSimprintsConstants
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
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
    private val vm by viewModels<ClientApiViewModel>()
    private val binding by viewBinding(FragmentClientApiBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<LoginResult>(
            viewLifecycleOwner,
            R.id.clientApiFragment,
            LoginContract.LOGIN_DESTINATION_ID,
        ) { result -> vm.handleLoginResult(result) }


        vm.proceedWithAction.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { action ->
            // TODO replace with proper flow
            findNavController().navigate(
                R.id.action_clientApiFragment_to_stubFragment,
                StubFragmentArgs(action.toString()).toBundle()
            )
        })

        vm.showAlert.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { error ->
            findNavController().navigate(
                R.id.action_clientApiFragment_to_alert,
                alertConfigurationMapper.buildAlertConfig(error)
                    // .withPayload() // TODO add payload ot differentiate alert screens when returning
                    .toArgs()
            )
        })

        vm.showLoginFlow.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { request ->
            findNavController().navigate(
                R.id.action_clientApiFragment_to_login,
                LoginContract.toArgs(request.projectId, request.userId),
            )
        })

        vm.returnResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { response ->
            val responseExtras = resultMapper(response)
            val resultCode = responseExtras.getInt(LibSimprintsConstants.RESULT_CODE_OVERRIDE, AppCompatActivity.RESULT_OK)
            responseExtras.remove(LibSimprintsConstants.RESULT_CODE_OVERRIDE)

            findNavController().finishWithResult(this, ClientApiResult(resultCode, responseExtras))
        })
    }

    override fun onResume() {
        super.onResume()

        if (!isActivityRestored && !requestProcessed) {
            requestProcessed = true
            lifecycleScope.launch {

                vm.handleIntent(args.requestAction, args.requestParams.toMap())
            }
        }
    }

    @Parcelize
    data class ClientApiResult(
        val resultCode: Int,
        val extras: Bundle,
    ) : Parcelable
}
