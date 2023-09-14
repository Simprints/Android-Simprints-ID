package com.simprints.face.configuration.screen

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.face.configuration.FaceConfigurationContract
import com.simprints.face.configuration.R
import com.simprints.face.configuration.databinding.ActivityFaceConfigurationWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@Keep
@AndroidEntryPoint
class FaceConfigurationWrapperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityFaceConfigurationWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.configurationHost.handleResult<Parcelable>(this, R.id.faceConfigurationFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(FaceConfigurationContract.RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(FACE_CONFIGURATION_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.configurationHost).setGraph(R.navigation.graph_face_configuration, args)
    }

    companion object {

        const val FACE_CONFIGURATION_ARGS_EXTRA = "subject_args_args"
    }
}
