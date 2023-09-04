package com.simprints.feature.clientapi.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.clientapi.R
import com.simprints.feature.clientapi.databinding.ActivityClientApiBinding
import com.simprints.feature.clientapi.models.ClientApiResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ClientApiActivity : BaseActivity() {

    private val binding by viewBinding(ActivityClientApiBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.clientApiHost.handleResult<ClientApiResult>(this, R.id.clientApiFragment) { result ->
            setResult(result.resultCode, Intent().putExtras(result.extras))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val action = intent.action.orEmpty()
        val extras = intent.extras ?: bundleOf()

        findNavController(R.id.clientApiHost).setGraph(
            R.navigation.graph_client_api,
            ClientApiFragmentArgs(action, extras).toBundle()
        )
    }

}
