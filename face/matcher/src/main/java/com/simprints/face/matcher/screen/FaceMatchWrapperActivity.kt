package com.simprints.face.matcher.screen

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.face.matcher.FaceMatchContract
import com.simprints.face.matcher.R
import com.simprints.face.matcher.databinding.ActivityFaceMatcherWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@Keep
@AndroidEntryPoint
class FaceMatchWrapperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityFaceMatcherWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.matcherHost.handleResult<Parcelable>(this, R.id.faceMatcherFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(FaceMatchContract.RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(FACE_MATCHER_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.matcherHost).setGraph(R.navigation.graph_face_matcher, args)
    }

    companion object {

        const val FACE_MATCHER_ARGS_EXTRA = "face_matcher_args"
    }
}
