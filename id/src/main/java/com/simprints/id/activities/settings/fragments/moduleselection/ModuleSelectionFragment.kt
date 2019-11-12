package com.simprints.id.activities.settings.fragments.moduleselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ChipClickListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ModuleChipHelper
import com.simprints.id.moduleselection.model.Module
import kotlinx.android.synthetic.main.fragment_module_selection.*
import javax.inject.Inject

class ModuleSelectionFragment(
    private val application: Application
) : Fragment(), ModuleSelectionListener, ChipClickListener {

    @Inject lateinit var viewModelFactory: ModuleViewModelFactory

    private val adapter by lazy { ModuleAdapter(listener = this) }

    private val chipHelper by lazy { ModuleChipHelper(requireContext(), listener = this) }

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ModuleViewModel::class.java)
    }

    private var modules = emptyList<Module>()
    private var selectedModules = mutableListOf<Module>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_module_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvModules.adapter = adapter
        application.component.inject(this)
        fetchData()
    }

    override fun onModuleSelected(module: Module) {
        module.isSelected = true
        selectedModules.add(module)
        saveSelection(module)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onChipClick(module: Module) {
        modules.findMatchFor(module)?.isSelected = false
        selectedModules.remove(module)
        saveSelection(module)
    }

    private fun fetchData() {
        viewModel.getModules().observe(this, Observer { modules ->
            this.modules = modules
            adapter.submitList(modules.getUnselected())
            configureSearchView()
            configureTextViewVisibility()
            displaySelectedModules()
            updateSelectedModules()
        })
    }

    private fun configureSearchView() {
        val queryListener = ModuleSelectionQueryListener(modules.getUnselected())
        searchView.setOnQueryTextListener(queryListener)
        observeSearchResults(queryListener)
    }

    private fun displaySelectedModules() {
        modules.filter(::isSelectedModuleNotDisplayed).forEach { selectedModule ->
            addChipForModule(selectedModule)
        }
    }

    private fun isSelectedModuleNotDisplayed(module: Module): Boolean {
        return module.isSelected && !selectedModules.contains(module)
    }

    private fun updateSelectedModules() {
        selectedModules = modules.getSelected().toMutableList()
    }

    private fun addChipForModule(selectedModule: Module) {
        val chip = chipHelper.createChipForModule(selectedModule)
        chipGroup.addView(chip)
    }

    private fun observeSearchResults(queryListener: ModuleSelectionQueryListener) {
        queryListener.searchResults.observe(this, Observer { searchResults ->
            adapter.submitList(searchResults)

            txtNoResults.visibility = if (searchResults.isEmpty()) VISIBLE else GONE
        })
    }

    private fun saveSelection(lastModuleChanged: Module) {
        val maxSelectedModules = viewModel.getMaxSelectedModules()
        val noModulesSelected = selectedModules.isEmpty()
        val tooManyModulesSelected = lastModuleChanged.isSelected
            && selectedModules.size > maxSelectedModules

        when {
            noModulesSelected -> handleNoModulesSelected(lastModuleChanged)

            tooManyModulesSelected -> handleTooManyModulesSelected(
                maxSelectedModules,
                lastModuleChanged
            )

            else -> handleModuleSelected(lastModuleChanged)
        }
    }

    private fun handleNoModulesSelected(lastModuleChanged: Module) {
        notifyNoModulesSelected()
        modules.findMatchFor(lastModuleChanged)?.isSelected = true
        selectedModules.add(lastModuleChanged)
    }

    private fun handleTooManyModulesSelected(maxSelectedModules: Int, lastModuleChanged: Module) {
        notifyTooManyModulesSelected(maxSelectedModules)
        modules.findMatchFor(lastModuleChanged)?.isSelected = false
        selectedModules.remove(lastModuleChanged)
    }

    private fun handleModuleSelected(lastModuleChanged: Module) {
        viewModel.updateModules(modules)

        if (lastModuleChanged.isSelected)
            addChipForModule(lastModuleChanged)
        else
            chipGroup.removeView(chipHelper.findSelectedChip(chipGroup))
    }

    private fun notifyNoModulesSelected() {
        Toast.makeText(
            application,
            R.string.settings_no_modules_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun notifyTooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            application,
            application.getString(R.string.settings_too_many_modules_toast, maxAllowed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun configureTextViewVisibility() {
        if (modules.none { it.isSelected }) {
            txtNoModulesSelected.visibility = VISIBLE
            txtSelectedModules.visibility = GONE
        } else {
            txtNoModulesSelected.visibility = GONE
            txtSelectedModules.visibility = VISIBLE
        }
    }

    private fun List<Module>.getSelected() = filter { it.isSelected }

    private fun List<Module>.getUnselected() = filter { !it.isSelected }

    private fun List<Module>.findMatchFor(module: Module) = find { it.name == module.name }

}
