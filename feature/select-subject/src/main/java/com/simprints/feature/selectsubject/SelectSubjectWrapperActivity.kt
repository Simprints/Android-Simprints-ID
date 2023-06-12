package com.simprints.feature.selectsubject

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.selectsubject.databinding.ActivityGuidActionWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint

// Wrapper activity must be public because it is being referenced by the classname from legacy orchestrator.
@Keep
@AndroidEntryPoint
class SelectSubjectWrapperActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityGuidActionWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.guidActionHost.handleResult<Parcelable>(this, R.id.selectSubjectFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(SelectSubjectContract.SELECT_SUBJECT_RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(SELECT_SUBJECT_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.guidActionHost).setGraph(R.navigation.graph_select_subject, args)
    }

    companion object {

        const val SELECT_SUBJECT_ARGS_EXTRA = "select_subject_args"
    }
}
