package com.simprints.feature.fetchsubject

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.navigation.findNavController
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.feature.fetchsubject.databinding.ActivityGuidActionWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

// Wrapper activity must be public because it is being referenced by the classname from legacy orchestrator.
@Keep
@AndroidEntryPoint
class FetchSubjectWrapperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityGuidActionWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.guidActionHost.handleResult<Parcelable>(this, R.id.fetchSubjectFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(FetchSubjectContract.FETCH_SUBJECT_RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(FETCH_SUBJECT_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.guidActionHost).setGraph(R.navigation.graph_fetch_subject, args)
    }

    companion object {

        const val FETCH_SUBJECT_ARGS_EXTRA = "subject_args_args"
    }
}
