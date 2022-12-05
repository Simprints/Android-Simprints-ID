package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import android.app.AlertDialog
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentSyncModuleSelectionBinding
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.adapter.ModuleAdapter
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.adapter.ModuleSelectionListener
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools.ChipClickListener
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools.ModuleChipHelper
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ModuleSelectionFragment : Fragment(R.layout.fragment_sync_module_selection),
    ModuleSelectionListener, ChipClickListener {

    private val adapter by lazy { ModuleAdapter(listener = this) }
    private val chipHelper by lazy {
        // We need to have material theme for the chip
        ModuleChipHelper(
            ContextThemeWrapper(
                requireContext(),
                IDR.style.AppTheme_NoActionBar_MaterialComponents
            ), this
        )

    }
    private val viewModel by viewModels<ModuleSelectionViewModel>()
    private val binding by viewBinding(FragmentSyncModuleSelectionBinding::bind)

    private var modulesToSelect = emptyList<Module>()
    private var rvModules: RecyclerView? = null

    private val confirmModuleSelectionDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(IDR.string.confirm_module_selection_title))
            .setMessage(getModulesSelectedTextForDialog())
            .setCancelable(false)
            .setPositiveButton(getString(IDR.string.confirm_module_selection_yes))
            { _, _ -> handleModulesConfirmClick() }
            .setNegativeButton(getString(IDR.string.confirm_module_selection_cancel))
            { _, _ -> findNavController().popBackStack() }
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            }
        )
    }

    override fun onModuleSelected(module: Module) {
        binding.searchView.setQuery("", false)
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

    private fun configureRecyclerView() {
        rvModules = binding.rvModules
        rvModules?.adapter = adapter
        val context = requireContext()
        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            .apply {
                val colour = ContextCompat.getColor(context, IDR.color.simprints_light_grey)
                setDrawable(ColorDrawable(colour))
            }
        rvModules?.addItemDecoration(dividerItemDecoration)
    }

    private fun fetchData() {
        viewModel.modulesList.observe(viewLifecycleOwner) {
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
        } catch (e: Exception) {
            if (e is NoModuleSelectedException) notifyNoModulesSelected()
            if (e is TooManyModulesSelectedException) notifyTooManyModulesSelected(e.maxNumberOfModules)
        }
    }

    private fun displaySelectedModules() {
        val displayedModuleNames = chipHelper.findSelectedModuleNames(binding.chipGroup)

        modulesToSelect.forEach { module ->
            val isModuleDisplayed = displayedModuleNames.contains(module.name)
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

    private fun getModulesSelectedTextForDialog() = StringBuilder().apply {
        modulesToSelect.filter { it.isSelected }.forEach { module ->
            append(module.name + "\n")
        }
    }.toString()

    private fun handleModulesConfirmClick() {
        viewModel.saveModules()
        findNavController().popBackStack()
    }

    private fun notifyNoModulesSelected() {
        Toast.makeText(
            requireContext(),
            IDR.string.settings_no_modules_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun notifyTooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            requireContext(),
            String.format(getString(IDR.string.settings_too_many_modules_toast), maxAllowed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun configureSearchView() {
        configureSearchViewEditText()
        binding.searchView.queryHint = getString(IDR.string.hint_search_modules)
        val queryListener = ModuleSelectionQueryListener(modulesToSelect.getUnselected())
        binding.searchView.setOnQueryTextListener(queryListener)
        observeSearchResults(queryListener)
    }

    private fun configureSearchViewEditText() {
        val editText: EditText? = requireActivity().findViewById(
            androidx.appcompat.R.id.search_src_text
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
        if (viewModel.hasSelectionChanged()) {
            confirmModuleSelectionDialog.show()
        } else {
            findNavController().popBackStack()
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
            if (!hasFocus)
                rvModules?.scrollToPosition(0)
            // The safe call above is necessary only when the 'up' action bar button is clicked
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rvModules = null
        if (confirmModuleSelectionDialog.isShowing) {
            confirmModuleSelectionDialog.dismiss()
        }
    }
}
