package com.simprints.feature.moduleselector

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simprints.feature.module.selector.R
import com.simprints.feature.module.selector.databinding.DialogModuleSelectorBinding
import com.simprints.feature.moduleselector.ModuleSelectorDialogState.SelectionError
import com.simprints.feature.moduleselector.adapter.ModuleSelectorAdapter
import com.simprints.infra.uibase.password.SettingsPasswordDialogFragment
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ModuleSelectorDialogFragment : BottomSheetDialogFragment(R.layout.dialog_module_selector) {
    private val binding by viewBinding(DialogModuleSelectorBinding::bind)
    private val viewModel: ModuleSelectorDialogViewModel by viewModels()

    private val adapter by lazy { ModuleSelectorAdapter { viewModel.onAction(ModuleSelectorDialogAction.ModuleClicked(it)) } }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

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
            lifecycleOwner = this@ModuleSelectorDialogFragment,
            onSuccess = { viewModel.onAction(ModuleSelectorDialogAction.UnlockScreen) },
        )
        moduleSelectionCancelButton.setOnClickListener {
            viewModel.onAction(ModuleSelectorDialogAction.CancelClicked)
        }
        moduleSelectionConfirmButton.setOnClickListener {
            viewModel.onAction(ModuleSelectorDialogAction.SaveClicked)
        }
        moduleSelectionToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onAction(ModuleSelectorDialogAction.OnlySelectedChanged(isChecked))
        }
        modulesLockOverlayClickableArea.setOnClickListener {
            viewModel.onAction(ModuleSelectorDialogAction.LockOverlayClicked)
        }
        moduleSelectionSearchInput.addTextChangedListener { text ->
            viewModel.onAction(ModuleSelectorDialogAction.SearchQueryChanged(text?.toString().orEmpty()))
        }
    }

    private fun renderState(state: ModuleSelectorDialogState) = with(binding) {
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

    private fun handleEffect(effect: ModuleSelectorDialogEffects) = when (effect) {
        ModuleSelectorDialogEffects.DismissDialog -> dismiss()
        is ModuleSelectorDialogEffects.ShowPasswordDialog -> {
            SettingsPasswordDialogFragment
                .newInstance(passwordToMatch = effect.password)
                .show(childFragmentManager, SettingsPasswordDialogFragment.TAG)
        }
    }
}
