package com.simprints.feature.consent.screens

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.R
import com.simprints.feature.consent.databinding.ActivityConsentWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint

// Wrapper activity must be public because it is being referenced by the classname from legacy orchestrator.
@Keep
@AndroidEntryPoint
class ConsentWrapperActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityConsentWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.consentHost.handleResult<Parcelable>(this, R.id.consentFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(ConsentContract.CONSENT_RESULT, result) })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val args = intent.extras?.getBundle(CONSENT_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.consentHost).setGraph(R.navigation.graph_consent, args)
    }

    companion object {

        const val CONSENT_ARGS_EXTRA = "consent_args"
    }
}
