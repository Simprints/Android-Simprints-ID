package com.simprints.feature.selectsubject.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.feature.selectsubject.R
import com.simprints.feature.selectsubject.SelectSubjectParams
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.navigationParams
import com.simprints.infra.uibase.view.applySystemBarInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SelectSubjectFragment : Fragment(R.layout.fragment_select_subject) {
    private val viewModel: SelectSubjectViewModel by viewModels()
    private val params: SelectSubjectParams by navigationParams()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("SelectSubjectFragment started", tag = ORCHESTRATION)

        viewModel.finish.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let(::finishWithResult)
        }
        viewModel.saveGuidSelection(params.projectId, params.subjectId)
    }

    private fun finishWithResult(success: Boolean) {
        findNavController().finishWithResult(this, SelectSubjectResult(success))
    }
}
