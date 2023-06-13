package com.simprints.feature.setup

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.setup.databinding.ActivitySetupWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint

// Wrapper activity must be public because it is being referenced by the classname from legacy orchestrator.
@Keep
@AndroidEntryPoint
class SetupWrapperActivity : AppCompatActivity() {


    private val binding by viewBinding(ActivitySetupWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.setupHost.handleResult<Parcelable>(this, R.id.setupFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(SetupContract.SETUP_RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(SETUP_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.setupHost).setGraph(R.navigation.graph_setup, args)
    }

    companion object {

        const val SETUP_ARGS_EXTRA = "setup_args"
    }
}
