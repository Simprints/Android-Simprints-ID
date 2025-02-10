package com.simprints.feature.selectsubject.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.feature.selectsubject.R
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SelectSubjectFragment : Fragment(R.layout.fragment_select_subject) {
    private val viewModel: SelectSubjectViewModel by viewModels()
    private val args: SelectSubjectFragmentArgs by navArgs()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("SelectSubjectFragment started", tag = ORCHESTRATION)

        viewModel.finish.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let(::finishWithResult)
        }
        viewModel.saveGuidSelection(args.projectId, args.subjectId)
    }

    private fun finishWithResult(success: Boolean) {
        findNavController().finishWithResult(this, SelectSubjectResult(success))
    }
}
