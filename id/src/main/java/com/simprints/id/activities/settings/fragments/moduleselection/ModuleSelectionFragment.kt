package com.simprints.id.activities.settings.fragments.moduleselection

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ChipClickListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ModuleChipHelper
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.extensions.hideKeyboard
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.fragment_module_selection.*
import org.jetbrains.anko.sdk27.coroutines.onEditorAction
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import javax.inject.Inject

class ModuleSelectionFragment(
    private val application: Application
) : Fragment(), ModuleSelectionListener, ChipClickListener {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var viewModelFactory: ModuleViewModelFactory
    @Inject lateinit var peopleSyncManager: PeopleSyncManager

    private val adapter by lazy { ModuleAdapter(listener = this) }

    private val chipHelper by lazy { ModuleChipHelper(requireContext(), listener = this) }

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ModuleViewModel::class.java)
    }

    private var modules = emptyList<Module>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_module_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        application.component.inject(this)

        configureRecyclerView()
        configureTextViews()
        fetchData()
    }

    private fun configureTextViews() {
        txtSelectedModules.text = androidResourcesHelper.getString(R.string.selected_modules)
        txtNoModulesSelected.text = androidResourcesHelper.getString(R.string.no_modules_selected)
        txtNoResults.text = androidResourcesHelper.getString(R.string.no_results)
    }

    override fun onModuleSelected(module: Module) {
        searchView.setQuery("", false)
        updateSelectionIfPossible(module)
        scrollView.post {
            scrollView.isSmoothScrollingEnabled = false
            scrollView.fullScroll(View.FOCUS_DOWN)
            scrollView.isSmoothScrollingEnabled = true
        }
        searchView.requestFocus()
    }

    private fun refreshSyncWorkers() {
        peopleSyncManager.stop()
        peopleSyncManager.sync()
    }

    override fun onChipClick(module: Module) {
        updateSelectionIfPossible(module)
    }

    private fun configureRecyclerView() {
        rvModules.adapter = adapter
        val context = requireContext()
        val dividerItemDecoration = DividerItemDecoration(context,
            DividerItemDecoration.VERTICAL).apply {
            val colour = ContextCompat.getColor(context, R.color.simprints_light_grey)
            setDrawable(ColorDrawable(colour))
        }
        rvModules.addItemDecoration(dividerItemDecoration)
    }

    private fun fetchData() {
        viewModel.modulesList.observe(viewLifecycleOwner, Observer { modules ->
            this.modules = modules
            adapter.submitList(modules.getUnselected())
            configureSearchView()
            configureTextViewVisibility()
            displaySelectedModules()
            rvModules.requestFocus()
        })
    }

    private fun configureSearchView() {
        configureSearchViewEditText()
        searchView.queryHint = androidResourcesHelper.getString(R.string.hint_search_modules)
        val queryListener = ModuleSelectionQueryListener(modules.getUnselected())
        searchView.setOnQueryTextListener(queryListener)
        observeSearchResults(queryListener)
    }

    private fun configureSearchViewEditText() {
        val editText = requireActivity().findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        with(editText) {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.muli)
            observeSearchButton()
            observeFocus()
        }
    }

    private fun displaySelectedModules() {
        val displayedModuleNames = chipHelper.findSelectedModuleNames(chipGroup)

        modules.forEach { module ->
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
        chipHelper.addModuleChip(chipGroup, selectedModule)
    }

    private fun removeChipForModule(selectedModule: Module) {
        chipHelper.removeModuleChip(chipGroup, selectedModule)
    }

    private fun observeSearchResults(queryListener: ModuleSelectionQueryListener) {
        queryListener.searchResults.observe(viewLifecycleOwner, Observer { searchResults ->
            adapter.submitList(searchResults)
            txtNoResults.visibility = if (searchResults.isEmpty()) VISIBLE else GONE
            rvModules.scrollToPosition(0)
        })
    }

    private fun updateSelectionIfPossible(lastModuleChanged: Module) {
        val maxSelectedModules = viewModel.getMaxNumberOfModules()

        val selectedModulesSize = modules.getSelected().size
        val noModulesSelected = lastModuleChanged.isSelected && selectedModulesSize == 1
        val tooManyModulesSelected = !lastModuleChanged.isSelected
            && selectedModulesSize == maxSelectedModules

        when {
            noModulesSelected -> notifyNoModulesSelected()
            tooManyModulesSelected -> notifyTooManyModulesSelected(maxSelectedModules)
            else -> handleModuleSelected(lastModuleChanged)
        }
    }

    private fun handleModuleSelected(lastModuleChanged: Module) {
        lastModuleChanged.isSelected = !lastModuleChanged.isSelected
        viewModel.updateModules(modules)
    }

    private fun notifyNoModulesSelected() {
        activity?.showToast(androidResourcesHelper, R.string.settings_no_modules_toast)
    }

    fun showModuleSelectionDialogIfNecessary() {
        if (isModuleSelectionChanged()) {
            activity?.runOnUiThreadIfStillRunning {
                buildConfirmModuleSelectionDialog().show()
            }
        } else {
            activity?.finish()
        }
    }

    private fun isModuleSelectionChanged() = with(modules.filter { it.isSelected }) {
        when {
            isEmpty() && preferencesManager.selectedModules.isEmpty() -> false
            else -> map { it.name }.toSet() != preferencesManager.selectedModules
        }
    }

    private fun buildConfirmModuleSelectionDialog() =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(androidResourcesHelper.getString(R.string.confirm_module_selection_title))
            .setMessage(getModulesSelectedTextForDialog())
            .setCancelable(false)
            .setPositiveButton(androidResourcesHelper.getString(R.string.confirm_module_selection_yes))
            { _, _ -> handleModulesConfirmClick() }
            .setNegativeButton(androidResourcesHelper.getString(R.string.confirm_module_selection_cancel))
            { _, _ -> handleModuleSelectionCancelClick() }
            .create()

    private fun getModulesSelectedTextForDialog() = StringBuilder().apply {
        modules.filter { it.isSelected }.forEach { module ->
            append(module.name + "\n")
        }
    }.toString()

    private fun handleModulesConfirmClick() {
        viewModel.saveModules(modules)
        refreshSyncWorkers()
        activity?.finish()
    }

    private fun handleModuleSelectionCancelClick() {
        viewModel.resetModules()
    }

    private fun notifyTooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            application,
            androidResourcesHelper.getString(R.string.settings_too_many_modules_toast, arrayOf(maxAllowed)),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun configureTextViewVisibility() {
        if (isNoModulesSelected()) {
            txtNoModulesSelected.visibility = VISIBLE
            txtSelectedModules.visibility = GONE
        } else {
            txtNoModulesSelected.visibility = GONE
            txtSelectedModules.visibility = VISIBLE
        }
    }

    private fun isNoModulesSelected() = modules.none { it.isSelected }

    private fun List<Module>.getSelected() = filter { it.isSelected }

    private fun List<Module>.getUnselected() = filter { !it.isSelected }

    private fun EditText.observeSearchButton() {
        onEditorAction { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                v?.clearFocus()
                rvModules.requestFocus()
            }
        }
    }

    private fun EditText.observeFocus() {
        onFocusChange { v, hasFocus ->
            (v as EditText).isCursorVisible = hasFocus
            if (!hasFocus)
                rvModules?.scrollToPosition(0)
            // The safe call above is necessary only when the 'up' action bar button is clicked
        }
    }

}
