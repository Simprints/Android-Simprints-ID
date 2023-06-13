package com.simprints.feature.enrollast

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.enrollast.databinding.ActivityEnrolLastWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint

// Wrapper activity must be public because it is being referenced by the classname from legacy orchestrator.
@Keep
@AndroidEntryPoint
class EnrolLastBiometricWrapperActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityEnrolLastWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.enrolLastHost.handleResult<Parcelable>(this, R.id.enrolLastBiometricFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(EnrolLastBiometricContract.ENROL_LAST_RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(ENROL_LAST_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.enrolLastHost).setGraph(R.navigation.graph_enrol_last, args)
    }

    companion object {

        const val ENROL_LAST_ARGS_EXTRA = "enrol_last_args"
    }
}
