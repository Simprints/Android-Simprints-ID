package com.simprints.feature.selectagegroup.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.feature.selectagegroup.R
import com.simprints.feature.selectagegroup.databinding.FragmentAgeGroupSelectionBinding
import com.simprints.feature.selectagegroup.SelectSubjectAgeResult
import com.simprints.infra.uibase.navigation.finishWithResult
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
        viewModel.createAgeGroups()

        viewModel.finish.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let(::finishWithResult)
        }
        //   viewModel.saveGuidSelection(args.projectId, args.subjectId)
    }

    private fun fillRecyclerView(ageGroupsList: List<AgeGroup>) {
        // fill the recycler view with the age groups
        // 0 to 6 months, 6 months to 5 years, 5 to 10 years, 10 years and above
        // on click of an age group, call viewModel.saveGuidSelection(args.projectId, args.subjectId)
        with(binding.ageGroupRecyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = DividerItemDecoration(
                this.context, (layoutManager as LinearLayoutManager).orientation
            )
            this.addItemDecoration(dividerItemDecoration)
            adapter = AgeGroupAdapter(ageGroupsList) {ageGroup ->
                 viewModel.saveAgeGroupSelection(ageGroup.range)
            }
        }
    }

    private fun finishWithResult(success: Boolean) {
        findNavController().finishWithResult(this, SelectSubjectAgeResult(success))
    }
}
