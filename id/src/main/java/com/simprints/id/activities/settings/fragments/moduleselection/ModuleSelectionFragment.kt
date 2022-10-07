package com.simprints.id.activities.settings.fragments.moduleselection

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ChipClickListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ModuleChipHelper
import com.simprints.id.databinding.FragmentModuleSelectionBinding
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.id.tools.extensions.showToast
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

class ModuleSelectionFragment : Fragment(R.layout.fragment_module_selection),
    ModuleSelectionListener, ChipClickListener {

    private val confirmModuleSelectionDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(IDR.string.confirm_module_selection_title))
            .setMessage(getModulesSelectedTextForDialog())
            .setCancelable(false)
            .setPositiveButton(getString(IDR.string.confirm_module_selection_yes))
            { _, _ -> handleModulesConfirmClick() }
            .setNegativeButton(getString(IDR.string.confirm_module_selection_cancel))
            { _, _ -> handleModuleSelectionCancelClick() }
            .create()
    }

    @Inject
    lateinit var viewModelFactory: ModuleSelectionViewModelFactory

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    private val adapter by lazy { ModuleAdapter(listener = this) }

    private val chipHelper by lazy { ModuleChipHelper(requireContext(), listener = this) }

    private val binding by viewBinding(FragmentModuleSelectionBinding::bind)
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ModuleSelectionViewModel::class.java]
    }

    private var modules = emptyList<Module>()
    private var modulesToSelect = emptyList<Module>()
    private var maxNumberOfModules = 0
    private var rvModules: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val component = (requireActivity().application as Application).component
        component.inject(this)

        configureRecyclerView()
        configureTextViews()
        fetchData()
    }

    private fun configureTextViews() {
        binding.apply {
            txtSelectedModules.text = getString(IDR.string.selected_modules)
            txtNoModulesSelected.text = getString(IDR.string.no_modules_selected)
            txtNoResults.text = getString(IDR.string.no_results)
        }
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

    private fun refreshSyncWorkers() {
        with(eventSyncManager) {
            stop()
            sync()
        }
    }

    override fun onChipClick(module: Module) {
        updateSelectionIfPossible(module)
    }

    private fun configureRecyclerView() {
        rvModules = binding.rvModules
        rvModules?.adapter = adapter
        val context = requireContext()
        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        ).apply {
            val colour = ContextCompat.getColor(context, IDR.color.simprints_light_grey)
            setDrawable(ColorDrawable(colour))
        }
        rvModules?.addItemDecoration(dividerItemDecoration)
    }

    private fun fetchData() {
        viewModel.modulesList.observe(viewLifecycleOwner) { modules ->
            this.modules = modules
            this.modulesToSelect = modules
            adapter.submitList(modules.getUnselected())
            configureSearchView()
            configureTextViewVisibility()
            displaySelectedModules()
            rvModules?.requestFocus()
        }

        viewModel.maxNumberOfModules.observe(viewLifecycleOwner) { maxNumberOfModules ->
            this.maxNumberOfModules = maxNumberOfModules
        }
    }

    private fun configureSearchView() {
        configureSearchViewEditText()
        binding.searchView.queryHint = getString(IDR.string.hint_search_modules)
        val queryListener = ModuleSelectionQueryListener(modules.getUnselected())
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

    private fun hideKeyboard() {
        requireActivity().hideKeyboard()
    }

    private fun addChipForModule(selectedModule: Module) {
        chipHelper.addModuleChip(binding.chipGroup, selectedModule)
    }

    private fun removeChipForModule(selectedModule: Module) {
        chipHelper.removeModuleChip(binding.chipGroup, selectedModule)
    }

    private fun observeSearchResults(queryListener: ModuleSelectionQueryListener) {
        queryListener.searchResults.observe(viewLifecycleOwner) { searchResults ->
            adapter.submitList(searchResults)
            binding.txtNoResults.visibility = if (searchResults.isEmpty()) VISIBLE else GONE
            rvModules?.scrollToPosition(0)
        }
    }

    private fun updateSelectionIfPossible(lastModuleChanged: Module) {
        val selectedModulesSize = modulesToSelect.getSelected().size
        val noModulesSelected = lastModuleChanged.isSelected && selectedModulesSize == 1
        val tooManyModulesSelected = !lastModuleChanged.isSelected
            && selectedModulesSize == maxNumberOfModules

        when {
            noModulesSelected -> notifyNoModulesSelected()
            tooManyModulesSelected -> notifyTooManyModulesSelected(maxNumberOfModules)
            else -> handleModuleSelected(lastModuleChanged)
        }
    }

    private fun handleModuleSelected(lastModuleChanged: Module) {
        lastModuleChanged.isSelected = !lastModuleChanged.isSelected
        viewModel.updateModules(modulesToSelect)
    }

    private fun notifyNoModulesSelected() {
        activity?.showToast(IDR.string.settings_no_modules_toast)
    }

    fun showModuleSelectionDialogIfNecessary() {
        if (isModuleSelectionChanged()) {
            activity?.runOnUiThreadIfStillRunning {
                confirmModuleSelectionDialog.show()
            }
        } else {
            activity?.finish()
        }
    }

    private fun isModuleSelectionChanged() = with(modulesToSelect.filter { it.isSelected }) {
        when {
            isEmpty() && modulesToSelect.isEmpty() -> false
            else -> map { it.name }.toSet() != modulesToSelect
        }
    }

    private fun getModulesSelectedTextForDialog() = StringBuilder().apply {
        modulesToSelect.filter { it.isSelected }.forEach { module ->
            append(module.name + "\n")
        }
    }.toString()

    private fun handleModulesConfirmClick() {
        viewModel.saveModules(modulesToSelect)
        refreshSyncWorkers()
        activity?.finish()
    }

    private fun handleModuleSelectionCancelClick() {
        viewModel.resetModules()
    }

    private fun notifyTooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            requireContext(),
            String.format(getString(IDR.string.settings_too_many_modules_toast), maxAllowed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun configureTextViewVisibility() {
        if (isNoModulesSelected()) {
            binding.txtNoModulesSelected.visibility = VISIBLE
            binding.txtSelectedModules.visibility = GONE
        } else {
            binding.txtNoModulesSelected.visibility = GONE
            binding.txtSelectedModules.visibility = VISIBLE
        }
    }

    private fun isNoModulesSelected() = modulesToSelect.none { it.isSelected }

    private fun List<Module>.getSelected() = filter { it.isSelected }

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
        rvModules = null
        super.onDestroyView()
        if (confirmModuleSelectionDialog.isShowing) {
            confirmModuleSelectionDialog.dismiss()
        }
    }
}
