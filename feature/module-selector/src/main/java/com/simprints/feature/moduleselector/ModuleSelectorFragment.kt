package com.simprints.feature.moduleselector

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.simprints.feature.module.selector.R
import com.simprints.feature.module.selector.databinding.DialogModuleSelectorBinding
import com.simprints.feature.moduleselector.ModuleSelectorState.SelectionError
import com.simprints.feature.moduleselector.adapter.ModuleSelectorAdapter
import com.simprints.infra.uibase.password.SettingsPasswordDialogFragment
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ModuleSelectorFragment : Fragment(R.layout.dialog_module_selector) {
    private val binding by viewBinding(DialogModuleSelectorBinding::bind)
    private val viewModel: ModuleSelectorViewModel by viewModels()

    private val adapter by lazy { ModuleSelectorAdapter { viewModel.onAction(ModuleSelectorAction.ModuleClicked(it)) } }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        applySystemBarInsets(view)
        configureRecyclerView()
        observeUi()
        setupListeners()
    }

    override fun onDestroyView() {
        binding.moduleSelectionRecyclerView.adapter = null
        super.onDestroyView()
    }

    private fun configureRecyclerView() = with(binding) {
        moduleSelectionRecyclerView.adapter = adapter
        moduleSelectionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::renderState) }
                launch { viewModel.effects.collect(::handleEffect) }
            }
        }
    }

    private fun setupListeners() = with(binding) {
        SettingsPasswordDialogFragment.registerForResult(
            fragmentManager = childFragmentManager,
            lifecycleOwner = this@ModuleSelectorFragment,
            onSuccess = { viewModel.onAction(ModuleSelectorAction.UnlockScreen) },
        )
        moduleSelectionToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        moduleSelectionCancelButton.setOnClickListener {
            viewModel.onAction(ModuleSelectorAction.CancelClicked)
        }
        moduleSelectionConfirmButton.setOnClickListener {
            viewModel.onAction(ModuleSelectorAction.SaveClicked)
        }
        moduleSelectionToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onAction(ModuleSelectorAction.OnlySelectedChanged(isChecked))
        }
        modulesLockOverlayClickableArea.setOnClickListener {
            viewModel.onAction(ModuleSelectorAction.LockOverlayClicked)
        }
        moduleSelectionSearchInput.addTextChangedListener { text ->
            viewModel.onAction(ModuleSelectorAction.SearchQueryChanged(text?.toString().orEmpty()))
        }
    }

    private fun renderState(state: ModuleSelectorState) = with(binding) {
        if (moduleSelectionToggleSwitch.isChecked != state.onlySelected) {
            // Avoids unnecessary selection listener update loops
            moduleSelectionToggleSwitch.isChecked = state.onlySelected
        }

        adapter.submitList(state.modules)
        moduleSelectionConfirmButton.isEnabled = state.isConfirmEnabled
        modulesLockOverlay.isVisible = state.isScreenLocked

        moduleSelectionErrorText.isVisible = state.selectionError != null
        moduleSelectionErrorText.text = when (state.selectionError) {
            null -> null
            SelectionError.NoModuleSelected -> getString(IDR.string.select_modules_no_modules_selected)
            is SelectionError.TooManyModulesSelected -> getString(
                IDR.string.select_modules_error_too_many_modules,
                state.selectionError.maxCount,
            )
        }
    }

    private fun handleEffect(effect: ModuleSelectorEffects) = when (effect) {
        ModuleSelectorEffects.Dismiss -> findNavController().popBackStack()
        is ModuleSelectorEffects.ShowPassword -> {
            SettingsPasswordDialogFragment
                .newInstance(passwordToMatch = effect.password)
                .show(childFragmentManager, SettingsPasswordDialogFragment.TAG)
        }
    }
}
