package com.simprints.feature.clientapi.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.clientapi.databinding.ClientStubActivityBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.infra.resources.R as IDR

/**
 * To be used as temporary destination for unfinished flows.
 *
 * TODO: Remove once orchestrator is done - https://simprints.atlassian.net/browse/CORE-2845
 */
class StubActivity : BaseActivity() {

    private val binding by viewBinding(ClientStubActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.stubTitle.text = intent.getStringExtra(EXTRA_MESSAGE)
            ?: getString(IDR.string.app_name)
    }

    companion object {
        fun getIntent(from: Context, message: String) = Intent(from, StubActivity::class.java)
            .putExtra(EXTRA_MESSAGE, message)

        private const val EXTRA_MESSAGE = "extra_message"
    }
}
