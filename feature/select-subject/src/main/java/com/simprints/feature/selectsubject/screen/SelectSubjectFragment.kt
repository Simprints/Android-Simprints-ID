package com.simprints.feature.selectsubject.screen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.extentions.nullIfEmpty
import com.simprints.feature.selectsubject.R
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.selectsubject.databinding.FragmentSelectSubjectBinding
import com.simprints.feature.selectsubject.model.SaveResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SelectSubjectFragment : Fragment(R.layout.fragment_select_subject) {
    private val viewModel: SelectSubjectViewModel by viewModels()
    private val args: SelectSubjectFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentSelectSubjectBinding::bind)

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
        viewModel.saveGuidSelection(
            projectId = args.projectId,
            subjectId = args.subjectId,
            externalCredentialId = args.externalCredentialId
        )

        if (args.externalCredentialId?.nullIfEmpty() != null) {
            binding.externalCredentialSavedCard.isVisible = true
        }
    }

    private fun finishWithResult(saveResult: SaveResult) {
        // [MS-985] Callout apps do not care about external credential saving result, they do not process it as of May 2025.
        // Refactor to pass the external credential save result if necessary.
        findNavController().finishWithResult(this, SelectSubjectResult(saveResult.isSubjectIdSaved))
    }
}
