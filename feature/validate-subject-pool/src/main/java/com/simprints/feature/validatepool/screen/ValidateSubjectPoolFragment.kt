package com.simprints.feature.validatepool.screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.simprints.feature.validatepool.R
import com.simprints.feature.validatepool.databinding.FragmentValidateSubjectPoolBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ValidateSubjectPoolFragment : Fragment(R.layout.fragment_validate_subject_pool) {

    private val viewModel: ValidateSubjectPoolViewModel by viewModels()
    private val binding by viewBinding(FragmentValidateSubjectPoolBinding::bind)
    private val args: ValidateSubjectPoolFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}
