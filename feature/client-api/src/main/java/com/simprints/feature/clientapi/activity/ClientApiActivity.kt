package com.simprints.feature.clientapi.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.alert.ShowAlertWrapper
import com.simprints.feature.alert.toArgs
import com.simprints.feature.clientapi.R
import com.simprints.feature.clientapi.extensions.toMap
import com.simprints.feature.clientapi.mappers.AlertConfigurationMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ClientApiActivity : BaseActivity(R.layout.activity_client_api) {

    private var isActivityRestored = false
    private var requestProcessed = false

    @Inject
    lateinit var alertConfigurationMapper: AlertConfigurationMapper

    private val vm by viewModels<ClientApiViewModel>()

    private val showAlert = registerForActivityResult(ShowAlertWrapper()) { data ->
        // TODO handle return from alert screen
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isActivityRestored = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.proceedWithAction.observe(this, LiveDataEventWithContentObserver { action ->
            // TODO replace with proper flow
            startActivity(StubActivity.getIntent(this, action.toString()))
        })

        vm.showAlert.observe(this, LiveDataEventWithContentObserver { error ->
            showAlert.launch(
                alertConfigurationMapper.buildAlertConfig(error)
                    // .withPayload() // TODO add payload ot differentiate alert screens when returning
                    .toArgs()
            )
        })
    }

    override fun onResume() {
        super.onResume()
        if (!isActivityRestored && !requestProcessed) {
            requestProcessed = true
            lifecycleScope.launch {
                // TODO check login state
                // TODO check root state

                vm.handleIntent(intent.action.orEmpty(), intent.extras?.toMap().orEmpty())
            }
        }
    }

}
