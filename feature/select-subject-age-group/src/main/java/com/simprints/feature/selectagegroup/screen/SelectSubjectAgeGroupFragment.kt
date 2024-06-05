package com.simprints.feature.selectagegroup.screen

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.feature.exitform.toArgs
import com.simprints.feature.selectagegroup.R
import com.simprints.feature.selectagegroup.SelectSubjectAgeResult
import com.simprints.feature.selectagegroup.databinding.FragmentAgeGroupSelectionBinding
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SelectSubjectAgeGroupFragment : Fragment(R.layout.fragment_age_group_selection) {

    private val viewModel: SelectSubjectAgeGroupViewModel by viewModels()
    private val binding by viewBinding(FragmentAgeGroupSelectionBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.ageGroups.observe(viewLifecycleOwner) { ageGroupsList ->
            fillRecyclerView(ageGroupsList)
        }

        viewModel.showExitForm.observe(viewLifecycleOwner) { exitFormConfig ->
            exitFormConfig.getContentIfNotHandled()?.let {
                findNavController().navigateSafely(
                    this, R.id.action_selectSubjectAgeGroupFragment_to_refusalFragment, it.toArgs()
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.onBackPressed()
        }

        viewModel.finish.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let(::finishWithResult)
        }
        viewModel.start()
    }

    private fun fillRecyclerView(ageGroupsList: List<AgeGroupDisplayModel>) {
        // fill the recycler view with the age groups
        // 0 to 6 months, 6 months to 5 years, 5 to 10 years, 10 years and above
        // on click of an age group, call viewModel.saveGuidSelection(args.projectId, args.subjectId)
        with(binding.ageGroupRecyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = DividerItemDecoration(
                this.context, (layoutManager as LinearLayoutManager).orientation
            )
            this.addItemDecoration(dividerItemDecoration)
            adapter = AgeGroupAdapter(ageGroupsList) { ageGroup ->
                viewModel.saveAgeGroupSelection(ageGroup.range)
            }
        }
    }

    private fun finishWithResult(ageGroup: AgeGroup) {
        findNavController().finishWithResult(this, SelectSubjectAgeResult(ageGroup))
    }
}
