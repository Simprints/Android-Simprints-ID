package com.simprints.id.activities.settings.fragments.moduleselection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionListener
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ModuleChipHelper
import com.simprints.id.moduleselection.model.Module
import kotlinx.android.synthetic.main.fragment_module_selection.*

class ModuleSelectionFragment(
    private val applicationContext: Context
) : Fragment(), ModuleSelectionListener, ModuleChipHelper.ChipClickListener {

    private val adapter by lazy { ModuleAdapter(listener = this) }

    private lateinit var viewModel: ModuleViewModel
    private lateinit var queryListener: ModuleSelectionQueryListener
    private lateinit var chipHelper: ModuleChipHelper

    private var modules = emptyList<Module>()
    private var selectedModules = mutableListOf<Module>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_module_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvModules.adapter = adapter
        viewModel = ViewModelProviders.of(this).get(ModuleViewModel::class.java)
        chipHelper = ModuleChipHelper(requireContext(), listener = this)
        fetchData()
    }

    override fun onModuleSelected(module: Module) {
        selectedModules.add(module)
        saveSelection(module)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onChipClick(module: Module) {
        modules.first {
            it.name == module.name
        }.isSelected = false
        selectedModules.remove(module)
        saveSelection(module)
    }

    private fun fetchData() {
        viewModel.getModules().observe(this, Observer { modules ->
            this.modules = modules
            adapter.submitList(modules.filter { !it.isSelected })
            configureSearchView()
            observeSearchResults()
            configureTextViewVisibility()
            displaySelectedModules()
            updateSelectedModules()
        })
    }

    private fun configureSearchView() {
        queryListener = ModuleSelectionQueryListener(modules)
        searchView.setOnQueryTextListener(queryListener)
    }

    private fun displaySelectedModules() {
        modules.filter {
            it.isSelected && !selectedModules.contains(it)
        }.forEach { selectedModule ->
            addChipForModule(selectedModule)
        }
    }

    private fun updateSelectedModules() {
        selectedModules = modules.filter { it.isSelected }.toMutableList()
    }

    private fun addChipForModule(selectedModule: Module) {
        val chip = chipHelper.createChipForModule(selectedModule)
        chipGroup.addView(chip)
    }

    private fun observeSearchResults() {
        queryListener.searchResults.observe(this, Observer { searchResults ->
            adapter.submitList(searchResults)

            txtNoResults.visibility = if (searchResults.isEmpty()) VISIBLE else GONE
        })
    }

    private fun saveSelection(lastModuleChanged: Module) {
        val maxSelectedModules = viewModel.getMaxSelectedModules()

        when {
            selectedModules.isEmpty() -> {
                noModulesSelected()
                modules.first { it.name == lastModuleChanged.name }.isSelected = true
                selectedModules.add(lastModuleChanged)
            }

            lastModuleChanged.isSelected && selectedModules.size > maxSelectedModules -> {
                tooManyModulesSelected(maxSelectedModules)
                modules.first { it.name == lastModuleChanged.name }.isSelected = false
                selectedModules.remove(lastModuleChanged)
            }

            else -> {
                viewModel.updateModules(modules)

                if (lastModuleChanged.isSelected)
                    addChipForModule(lastModuleChanged)
                else
                    chipGroup.removeView(chipHelper.findSelectedChip(chipGroup))
            }
        }
    }

    private fun noModulesSelected() {
        Toast.makeText(
            applicationContext,
            R.string.settings_no_modules_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun tooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.settings_too_many_modules_toast, maxAllowed),
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

}
