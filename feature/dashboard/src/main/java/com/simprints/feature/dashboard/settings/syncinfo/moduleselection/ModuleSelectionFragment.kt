package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSyncModuleSelectionBinding
import com.simprints.feature.dashboard.settings.password.SettingsPasswordDialogFragment
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.adapter.ModuleAdapter
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.adapter.ModuleSelectionListener
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools.ChipClickListener
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools.ModuleChipHelper
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ModuleSelectionFragment :
    Fragment(R.layout.fragment_sync_module_selection),
    ModuleSelectionListener,
    ChipClickListener {
    private val adapter by lazy { ModuleAdapter(listener = this) }
    private val chipHelper by lazy {
        // We need to have material theme for the chip
        ModuleChipHelper(ContextThemeWrapper(requireContext(), null), this)
    }

    private val viewModel by viewModels<ModuleSelectionViewModel>()
    private val binding by viewBinding(FragmentSyncModuleSelectionBinding::bind)

    private var hasModulesSelectedInitially: Boolean? = null
    private var modulesToSelect = emptyList<Module>()
    private var rvModules: RecyclerView? = null

    private val confirmModuleSelectionDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(IDR.string.dashboard_select_modules_confirm_title))
            .setMessage(getModulesSelectedTextForDialog())
            .setCancelable(false)
            .setPositiveButton(getString(IDR.string.dashboard_select_modules_confirm_yes)) { _, _ -> handleModulesConfirmClick() }
            .setNegativeButton(getString(IDR.string.dashboard_select_modules_confirm_no)) { _, _ -> findNavController().popBackStack() }
            .create()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        configureOverlay()
        configureRecyclerView()
        fetchData()
        binding.dashboardToolbar.setNavigationOnClickListener {
            onBackPress()
        }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPress()
                }
            },
        )
    }

    private fun configureOverlay() {
        viewModel.loadPasswordSettings()
        viewModel.screenLocked.observe(viewLifecycleOwner) {
            binding.modulesLockOverlay.isVisible = it?.locked == true
        }
        SettingsPasswordDialogFragment.registerForResult(
            fragmentManager = childFragmentManager,
            lifecycleOwner = this,
            onSuccess = { viewModel.unlockScreen() },
        )
        binding.modulesLockOverlayClickableArea.setOnClickListener {
            val password = viewModel.screenLocked.value?.getNullablePassword()
            if (password != null) {
                SettingsPasswordDialogFragment
                    .newInstance(
                        passwordToMatch = password,
                    ).show(childFragmentManager, SettingsPasswordDialogFragment.TAG)
            }
        }
    }

    override fun onModuleSelected(module: Module) {
        binding.searchViewInput.setText("")
        hideKeyboard()
        updateSelectionIfPossible(module)
        binding.scrollView.post {
            binding.scrollView.isSmoothScrollingEnabled = false
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
            binding.scrollView.isSmoothScrollingEnabled = true
        }
    }

    override fun onChipClick(module: Module) {
        updateSelectionIfPossible(module)
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    private fun configureRecyclerView() {
        rvModules = binding.rvModules
        rvModules?.adapter = adapter
        val context = requireContext()
        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            .apply {
                val colour = ContextCompat.getColor(context, IDR.color.simprints_grey_light)
                setDrawable(ColorDrawable(colour))
            }
        rvModules?.addItemDecoration(dividerItemDecoration)
    }

    private fun fetchData() {
        viewModel.modulesList.observe(viewLifecycleOwner) {
            if (hasModulesSelectedInitially == null) {
                hasModulesSelectedInitially = it.any(Module::isSelected)
            }
            modulesToSelect = it
            adapter.submitList(it.getUnselected())
            configureSearchView()
            configureTextViewVisibility()
            displaySelectedModules()
            rvModules?.requestFocus()
        }
    }

    private fun observeSearchResults(queryListener: ModuleSelectionQueryListener) {
        queryListener.searchResults.observe(viewLifecycleOwner) { searchResults ->
            adapter.submitList(searchResults)
            binding.txtNoResults.visibility =
                if (searchResults.isEmpty()) View.VISIBLE else View.GONE
            rvModules?.scrollToPosition(0)
        }
    }

    private fun updateSelectionIfPossible(lastModuleChanged: Module) {
        try {
            viewModel.updateModuleSelection(lastModuleChanged)
        } catch (e: TooManyModulesSelectedException) {
            notifyTooManyModulesSelected(e.maxNumberOfModules)
        }
    }

    private fun displaySelectedModules() {
        val displayedModuleNames = chipHelper.findSelectedModuleNames(binding.chipGroup)

        modulesToSelect.forEach { module ->
            val isModuleDisplayed = displayedModuleNames.contains(module.name.value)
            val isModuleSelected = module.isSelected

            when {
                isModuleSelected && !isModuleDisplayed -> addChipForModule(module)
                !isModuleSelected && isModuleDisplayed -> {
                    removeChipForModule(module)
                    hideKeyboard()
                }
            }
        }
    }

    private fun getModulesSelectedTextForDialog() = StringBuilder()
        .apply {
            modulesToSelect.filter { it.isSelected }.forEach { module ->
                append(module.name.value + "\n")
            }
        }.toString()

    private fun handleModulesConfirmClick() {
        try {
            viewModel.saveModules()
            findNavController().popBackStack()
        } catch (e: NoModuleSelectedException) {
            notifyNoModulesSelected()
        }
    }

    private fun notifyNoModulesSelected() {
        Toast
            .makeText(
                requireContext(),
                IDR.string.dashboard_select_modules_no_modules,
                Toast.LENGTH_SHORT,
            ).show()
    }

    private fun notifyTooManyModulesSelected(maxAllowed: Int) {
        Toast
            .makeText(
                requireContext(),
                String.format(
                    getString(IDR.string.dashboard_select_modules_too_many_modules),
                    maxAllowed,
                ),
                Toast.LENGTH_SHORT,
            ).show()
    }

    private fun configureSearchView() {
        configureSearchViewEditText()

        val queryListener = ModuleSelectionQueryListener(modulesToSelect.getUnselected())
        binding.searchViewInput.addTextChangedListener(queryListener)
        observeSearchResults(queryListener)
    }

    private fun configureSearchViewEditText() {
        val editText: EditText? = requireActivity().findViewById(
            androidx.appcompat.R.id.search_src_text,
        )

        editText?.let {
            it.typeface = try {
                ResourcesCompat.getFont(requireContext(), IDR.font.muli)
            } catch (ex: Exception) {
                Typeface.DEFAULT
            }
            it.observeSearchButton()
            it.observeFocus()
        }
    }

    private fun addChipForModule(selectedModule: Module) {
        chipHelper.addModuleChip(binding.chipGroup, selectedModule)
    }

    private fun removeChipForModule(selectedModule: Module) {
        chipHelper.removeModuleChip(binding.chipGroup, selectedModule)
    }

    private fun configureTextViewVisibility() {
        if (isNoModulesSelected()) {
            binding.txtNoModulesSelected.visibility = View.VISIBLE
            binding.txtSelectedModules.visibility = View.GONE
        } else {
            binding.txtNoModulesSelected.visibility = View.GONE
            binding.txtSelectedModules.visibility = View.VISIBLE
        }
    }

    private fun onBackPress() {
        when {
            isNoModulesSelected() && hasModulesSelectedInitially == true -> notifyNoModulesSelected()
            viewModel.hasSelectionChanged() -> confirmModuleSelectionDialog.show()
            else -> findNavController().popBackStack()
        }
    }

    private fun hideKeyboard() {
        requireActivity().hideKeyboard()
    }

    private fun isNoModulesSelected() = modulesToSelect.none { it.isSelected }

    private fun List<Module>.getUnselected() = filter { !it.isSelected }

    private fun EditText.observeSearchButton() {
        setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                v?.clearFocus()
                rvModules?.requestFocus()
            }
            false
        }
    }

    private fun EditText.observeFocus() {
        setOnFocusChangeListener { v, hasFocus ->
            (v as EditText).isCursorVisible = hasFocus
            if (!hasFocus) {
                rvModules?.scrollToPosition(0)
            }
            // The safe call above is necessary only when the 'up' action bar button is clicked
        }
    }

    override fun onDestroyView() {
        rvModules = null
        if (confirmModuleSelectionDialog.isShowing) {
            confirmModuleSelectionDialog.dismiss()
        }
        super.onDestroyView()
    }
}
