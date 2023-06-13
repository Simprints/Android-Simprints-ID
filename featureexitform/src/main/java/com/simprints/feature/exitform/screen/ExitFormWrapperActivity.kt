package com.simprints.feature.exitform.screen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.R
import com.simprints.feature.exitform.databinding.ActivityExitFormWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ExitFormWrapperActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityExitFormWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.exitFormHost.handleResult<ExitFormResult>(this, ExitFormContract.DESTINATION_ID) { result ->
            // Pass the fragment results directly into activity results
            setResult(RESULT_OK, Intent().also { it.putExtra(EXIT_FORM_RESULT, result) })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val args = intent.extras?.getBundle(EXIT_FORM_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.exitFormHost).setGraph(R.navigation.graph_exit_form, args)
    }

    companion object {

        internal const val EXIT_FORM_ARGS_EXTRA = "exit_form_args"
        internal const val EXIT_FORM_RESULT = "exit_form_fragment_result"
    }
}
