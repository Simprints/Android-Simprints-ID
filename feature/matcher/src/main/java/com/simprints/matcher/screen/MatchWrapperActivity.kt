package com.simprints.matcher.screen

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.matcher.MatchContract
import com.simprints.matcher.R
import com.simprints.matcher.databinding.ActivityMatcherWrapperBinding
import dagger.hilt.android.AndroidEntryPoint

@Keep
@AndroidEntryPoint
class MatchWrapperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMatcherWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.matcherHost.handleResult<Parcelable>(this, R.id.matcherFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(MatchContract.RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(MATCHER_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.matcherHost).setGraph(R.navigation.graph_matcher, args)
    }

    companion object {

        const val MATCHER_ARGS_EXTRA = "matcher_args"
    }
}
