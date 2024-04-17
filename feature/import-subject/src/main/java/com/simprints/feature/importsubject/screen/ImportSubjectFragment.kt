package com.simprints.feature.importsubject.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.feature.importsubject.ImportSubjectResult
import com.simprints.feature.importsubject.R
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ImportSubjectFragment : Fragment(R.layout.fragment_subject_import) {

    private val viewModel: ImportSubjectViewModel by viewModels()
    private val args: ImportSubjectFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.importSubjectFragment,
            AlertContract.DESTINATION
        ) { finishWithResult(false) }

        viewModel.subjectState.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            handleImportState(it)
        })

        viewModel.onViewCreated(args.projectId, args.subjectId)
    }

    private fun handleImportState(state: ImportSubjectState) = when (state) {
        is ImportSubjectState.Imported -> finishWithResult(true)
        is ImportSubjectState.Error -> openAlert()
    }

    private fun openAlert() {
        findNavController().navigateSafely(
            this,
            R.id.action_fetchSubjectFragment_to_errorFragment,
            alertConfiguration {
                color = AlertColor.Gray
                title = "Failed to import subject"
                appErrorReason = AppErrorReason.UNEXPECTED_ERROR
            }.toArgs()
        )
    }

    private fun finishWithResult(complete: Boolean) {
        findNavController().finishWithResult(this, ImportSubjectResult(complete))
    }

}
